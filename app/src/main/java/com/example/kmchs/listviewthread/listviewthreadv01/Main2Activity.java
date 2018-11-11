package com.example.kmchs.listviewthread.listviewthreadv01;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private OnBackPressedExit onBackPressedExit;
    RequestQueue myRequestQueue;
    Document doc=null;
    Document episode=null;
    Elements titles;
    Elements dates;
    Elements rawdata;
    Elements episodeTitle;
    Elements episodeUrl;
    ArrayList<String> titleString;
    ArrayList<String> dateString;
    ArrayList<String> thumbnailString;
    ArrayList<String> urlString;
    ArrayList<String> episodeString;
    ArrayList<String> episodeUrlString;
    String url="https://bgmfl.com/comics?";
    EditText edittext;
    ProgressBar progressBar;
    RelativeLayout mLayout;
    int page=2;
    int searchPage=1;
    boolean isFirstRefresh=true;
    int searchState=0;
    boolean isUsingData=false;
    boolean dataSaver=false;
    SharedPreferences sharedPref;
    PrefManager prefManager;
    Context context;
    String genre=null;
    String iconTemp=null;
    String urlTemp=null;
    final int nowVersion=1001;

    ListView listview;
    ListViewAdapter adapter;
    View footer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dataSaver= sharedPref.getBoolean("thumbnail",true);
        Context context=getApplicationContext();
        prefManager=new PrefManager(this);

        if(!sharedPref.getBoolean("blackTheme",false))
            setContentView(R.layout.activity_main2);
        else {
            setContentView(R.layout.activity_main2_dark);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkSecondary));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("머루머루");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        onBackPressedExit=new OnBackPressedExit(this);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                Toast.makeText(this, "데이터를 사용중입니다.", Toast.LENGTH_SHORT).show();
                isUsingData=true;
            }
        } else {
            // not connected to the internet
            Toast.makeText(this, "네트워크가 꺼져있습니다.", Toast.LENGTH_SHORT).show();
        }
        myRequestQueue = Volley.newRequestQueue(this);

        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh("page=","1",true);
                searchState=0;
                page=2;
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        //리스트뷰 구현
        if(!sharedPref.getBoolean("blackTheme",false))
            footer = getLayoutInflater().inflate(R.layout.listview_footer, null, false);
        else
            footer=getLayoutInflater().inflate(R.layout.listview_footer_dark, null, false);
        listview=(ListView) findViewById(R.id.listview1);
        listview.addFooterView(footer);
        listview.setAdapter(adapter);
        adapter=new ListViewAdapter(this);
        footer.setVisibility(View.GONE);

        Button button=(Button)findViewById(R.id.button);
        TextView moretext=(TextView)findViewById(R.id.moretext);
        edittext=(EditText)findViewById(R.id.edit);
        progressBar=(ProgressBar) findViewById(R.id.progress);


        //처음 새로고침
        refresh("page=","1",false);
        Log.e("refresh finished","refresh");

        //더보기 클릭리스너
        moretext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchState==0) {
                    refresh("page=", String.valueOf(page), false);
                    page++;
                }
                else if(searchState==1) {
                    refresh("search_val=",edittext.getText().toString()+"&page="+String.valueOf(searchPage),false);
                    searchPage++;
                }
                else {
                    refresh("search_val=",genre+"&page="+String.valueOf(searchPage),false);
                    searchPage++;
                }
            }
        });

        //검색버튼 클릭리스너
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh("search_val=", edittext.getText().toString(), true);
                searchState=1;
                searchPage=2;
            }
        });
        // 에딧텍스트 엔터리스너
        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        refresh("search_val=", edittext.getText().toString(), true);
                        searchState=1;
                        searchPage=2;
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        break;
                    default:

                        return false;
                }
                return true;
            }
        });
        // 리스트뷰 클릭리스너
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;

                episodeString=new ArrayList<String>();
                episodeUrlString=new ArrayList<String>();
                progressBar.setVisibility(View.VISIBLE);
                //히스토리 preferences에 넘겨줄 아이콘 url, 만화 url 저장
                iconTemp=item.getIcon();
                urlTemp=item.getUrl();
                //만화 화수 불러오기
                StringRequest epRequest=new StringRequest(Request.Method.GET, item.getUrl(), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        episode=Jsoup.parse(response);
                        episodeTitle=episode.select("span.title");
                        episodeUrl=episode.select("li.en_btns");
                        for(Element element: episodeTitle) {
                            episodeString.add(element.text());
                        }
                        for(Element element: episodeUrl)
                        {
                            Elements url;
                            url=element.select("a");
                            episodeUrlString.add(url.first().attr("id"));
                        }
                        progressBar.setVisibility(View.GONE);

                        show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                myRequestQueue.add(epRequest);
            }
        }) ;

        //업데이트 체크
        StringRequest updateCheck=new StringRequest(Request.Method.GET, "https://raw.githubusercontent.com/iidxcat/MalluMallu/master/version.txt",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String rawVersion=Jsoup.parse(response).text();

                        StringBuilder updateBuilder=new StringBuilder(rawVersion);
                        updateBuilder.delete(0,5);
                        int isForceUpdate=Integer.parseInt(updateBuilder.toString());
                        updateBuilder=new StringBuilder(rawVersion);
                        updateBuilder.delete(4,6);

                        int lastVersion=Integer.parseInt(updateBuilder.toString());
                        if(nowVersion<lastVersion)
                            Toast.makeText(Main2Activity.this, "업데이트가 있습니다.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(Main2Activity.this, "최신버전 입니다.", Toast.LENGTH_SHORT).show();

                        if(isForceUpdate==1) {
                            Toast.makeText(Main2Activity.this, "업데이트 후 실행해주세요.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                },null);
        myRequestQueue.add(updateCheck);
    }

    //키보드숨기기
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    //새로고침 및 더보기 메소드
    public void refresh(String type,String value, boolean isClear)
    {
        progressBar.setVisibility(View.VISIBLE);
        if(isClear)
        {
            adapter=null;
            adapter=new ListViewAdapter(this);
            listview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        titleString=new ArrayList<String>();
        dateString=new ArrayList<String>();
        urlString=new ArrayList<String>();
        thumbnailString=new ArrayList<String>();

        for(int i=0; i<32; i++) {
            titleString.add(null);
            dateString.add(null);
            thumbnailString.add(null);
            urlString.add(null);
        }
        Log.e("parsing started","parsing started");
        StringRequest myRequest=new StringRequest(Request.Method.GET, url+type+value,
                new Response.Listener<String>() {
                    int count=0;
                    @Override
                    public void onResponse(String response) {
                        doc=Jsoup.parse(response);
                        titles = doc.select("div.dotdotdot");
                        dates = doc.select("span.upc");
                        rawdata = doc.select("li.li_ct");
                        for(Element element: titles) {
                            titleString.set(count,element.text());
                            count++;
                        }
                        count=0;
                        for(Element element: dates) {
                            dateString.set(count,element.text());
                            count++;
                        }
                        count=0;
                        for(Element element: rawdata)
                        {
                            Elements pic;
                            Elements urls;
                            pic=element.select("img");
                            urls=element.select("a");
                            thumbnailString.set(count, pic.first().attr("src"));
                            urlString.set(count,"https://bgmfl.com/"+urls.first().attr("href"));
                            Log.e("url:"+urlString.get(count),"url:"+urlString.get(count));
                            count++;
                        }
                        count=0;
                        Log.e("log","parsing finished");

                        if(isUsingData && dataSaver)
                            for(int i=0; i<32; i++)
                            {
                                thumbnailString.set(i,"file:///android_asset/error.png");
                            }
                        for (int i = 0; i < 32; i++) {
                            if(titleString.get(i)!=null)
                            {
                                adapter.addItem(thumbnailString.get(i), titleString.get(i), dateString.get(i),urlString.get(i));
                            }
                        }
                        if(titleString.get(0)==null)
                            Toast.makeText(Main2Activity.this, "검색 결과 없거나 더 불러올 목록이 없습니다", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        if(isFirstRefresh) {
                            listview.setAdapter(adapter);
                            isFirstRefresh=false;
                        }
                        progressBar.setVisibility(View.GONE);
                        footer.setVisibility(View.VISIBLE);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error!","Text Parsing Error");
                progressBar.setVisibility(View.GONE);
                footer.setVisibility(View.VISIBLE);
            }
        });
        myRequestQueue.add(myRequest);

    }

    //화수 팝업
    void show()
    {
        final CharSequence[] items =  episodeString.toArray(new String[ episodeString.size()]);
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("선택");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                String selectedText = items[pos].toString();
                Intent intent=new Intent(Main2Activity.this, ViewerActivity.class);
                intent.putExtra("pos",pos);
                intent.putExtra("Title",selectedText);
                intent.putExtra("ID",episodeUrlString.get(pos));
                if(pos==0) {intent.putExtra("next",false);}
                else {intent.putExtra("next", true);}
                if(pos==episodeString.size()-1) {intent.putExtra("previous",false);}
                else {intent.putExtra("previous",true);}
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA);
                String time = sdfNow.format(new Date(System.currentTimeMillis()));
                prefManager.addData(iconTemp, selectedText, time, urlTemp);
                Log.e("history added","history added"+iconTemp+selectedText+time+urlTemp);
                startActivityForResult(intent,1);
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(resultCode==RESULT_OK)
        {
            if(requestCode==1)
            {
                int pos=data.getIntExtra("pos",0);
                switch (data.getIntExtra("data",0)){
                    case 0:
                        break;
                    case 1:
                        pos--;
                        Intent intent=new Intent(Main2Activity.this, ViewerActivity.class);
                        intent.putExtra("pos",pos);
                        intent.putExtra("Title",episodeString.get(pos));
                        intent.putExtra("ID",episodeUrlString.get(pos));
                        if(pos==0) {intent.putExtra("next",false);}
                        else {intent.putExtra("next", true);}
                        if(pos==episodeString.size()) {intent.putExtra("previous",false);}
                        else {intent.putExtra("previous",true);}

                        SharedPreferences pref=getSharedPreferences("pref1",Context.MODE_PRIVATE);
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA);
                        String time = sdfNow.format(new Date(System.currentTimeMillis()));
                        prefManager.addSingleData(episodeString.get(pos),time);

                        startActivityForResult(intent,1);
                        break;
                    case 2:
                        pos++;
                        Intent intentp=new Intent(Main2Activity.this, ViewerActivity.class);
                        intentp.putExtra("pos",pos);
                        intentp.putExtra("Title",episodeString.get(pos));
                        intentp.putExtra("ID",episodeUrlString.get(pos));
                        if(pos==0) {intentp.putExtra("next",false);}
                        else {intentp.putExtra("next", true);}
                        if(pos==episodeString.size()-1) {intentp.putExtra("previous",false);}
                        else {intentp.putExtra("previous",true);}

                        SharedPreferences pref2=getSharedPreferences("pref1",Context.MODE_PRIVATE);
                        SimpleDateFormat sdfNow2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA);
                        String time2 = sdfNow2.format(new Date(System.currentTimeMillis()));
                        prefManager.addSingleData(episodeString.get(pos),time2);

                        startActivityForResult(intentp,1);
                        break;

                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(searchState!=0) {
            refresh("page=","1",true);
            searchState=0;
            page=2;
            edittext.setText(null);
        }
        else {
            // BackPressedForFinish 클래스의 onBackPressed() 함수를 호출한다.
            onBackPressedExit.onBackPressed();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId()) {
            case R.id.menu_settings:
                Intent intent=new Intent(Main2Activity.this,SettingActivity.class);
                startActivity(intent);

                return true;
            case R.id.menu_exit:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.history)
        {
            searchState=3;
            adapter=null;
            adapter=new ListViewAdapter(this);
            listview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            footer.setVisibility(View.GONE);
            SharedPreferences pref;
            for(int i=1; i<33; i++)
            {
                pref=getSharedPreferences("pref"+String.valueOf(i),Context.MODE_PRIVATE);
                if(pref.getString("title",null)!=null) {
                    String tmb=pref.getString("thumbnailUrl",null);
                    String titl=pref.getString("title",null);
                    String dat=pref.getString("date",null);
                    String ur=pref.getString("url",null);
                adapter.addItem(tmb,titl,dat,ur);
                }
            }
            Toast.makeText(this, "기록은 총 32개만 저장됩니다.", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.d1) {
            refresh("search_val=","*완결", true);
            searchState=2;
            genre="*완결";
            searchPage=2;
            edittext.setText(null);
        } else if (id == R.id.d2) {
            refresh("search_val=","*액션", true);
            searchState=2;
            genre="*액션";
            searchPage=2;
            edittext.setText(null);
        } else if (id == R.id.d3) {
            refresh("search_val=","*이세계", true);
            searchState=2;
            genre="*이세계";
            searchPage=2;
            edittext.setText(null);
        } else if (id == R.id.d4) {
            refresh("search_val=","*일상치유", true);
            searchState=2;
            genre="*일상치유";
            searchPage=2;
            edittext.setText(null);
        } else if (id == R.id.d5) {
            refresh("search_val=","*전생", true);
            searchState=2;
            genre="*전생";
            searchPage=2;
            edittext.setText(null);
        } else if (id == R.id.d6) {
            refresh("search_val=","*추리", true);
            searchState=2;
            genre="*추리";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d7) {
            refresh("search_val=","*판타지", true);
            searchState=2;
            genre="*판타지";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d8) {
            refresh("search_val=","*학원", true);
            searchState=2;
            genre="*학원";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d9) {
            refresh("search_val=","*공포", true);
            searchState=2;
            genre="*공포";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d10) {
            refresh("search_val=","*개그", true);
            searchState=2;
            genre="*개그";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d11) {
            refresh("search_val=","*게임", true);
            searchState=2;
            genre="*게임";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d12) {
            refresh("search_val=","*도박", true);
            searchState=2;
            genre="*도박";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d13) {
            refresh("search_val=","*드라마", true);
            searchState=2;
            genre="*드라마";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d14) {
            refresh("search_val=","*라노벨", true);
            searchState=2;
            genre="*라노벨";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d15) {
            refresh("search_val=","*러브코미디", true);
            searchState=2;
            genre="*러브코미디";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d16) {
            refresh("search_val=","*먹방", true);
            searchState=2;
            genre="*먹방";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d17) {
            refresh("search_val=","*백합", true);
            searchState=2;
            genre="*백합";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d18) {
            refresh("search_val=","*여장", true);
            searchState=2;
            genre="*여장";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d19) {
            refresh("search_val=","*순정", true);
            searchState=2;
            genre="*순정";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d20) {
            refresh("search_val=","*스릴러", true);
            searchState=2;
            genre="*스릴러";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d21) {
            refresh("search_val=","*스포츠", true);
            searchState=2;
            genre="*스포츠";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d22) {
            refresh("search_val=","*17", true);
            searchState=2;
            genre="*17";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d23) {
            refresh("search_val=","*BL", true);
            searchState=2;
            genre="*BL";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d24) {
            refresh("search_val=","*역사", true);
            searchState=2;
            genre="*역사";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d25) {
            refresh("search_val=","*SF", true);
            searchState=2;
            genre="*SF";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d26) {
            refresh("search_val=","*TS", true);
            searchState=2;
            genre="*TS";
            searchPage=2;
            edittext.setText(null);
        }
        else if (id == R.id.d27) {
            refresh("search_val=","*애니화", true);
            searchState=2;
            genre="*애니화";
            searchPage=2;
            edittext.setText(null);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
