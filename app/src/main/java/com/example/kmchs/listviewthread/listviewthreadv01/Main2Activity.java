package com.example.kmchs.listviewthread.listviewthreadv01;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;

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
    boolean isSearch=false;
    boolean isUsingData=false;
    boolean dataSaver=false;
    SharedPreferences sharedPref;
    Context context;

    ListView listview;
    ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
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

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dataSaver= sharedPref.getBoolean("thumbnail",true);
        Context context=getApplicationContext();


        /*if(!sharedPref.getBoolean("blackTheme",false))
            setContentView(R.layout.activity_main2);
        else {
            //setContentView(R.layout.main_dark);
            setContentView(R.layout.activity_main2);
            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkPrimary)));
            //getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkSecondary));
        }*/
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
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        //리스트뷰 구현
        View footer = getLayoutInflater().inflate(R.layout.listview_footer, null, false);
        listview=(ListView) findViewById(R.id.listview1);
        listview.addFooterView(footer);
        listview.setAdapter(adapter);
        adapter=new ListViewAdapter(this);

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
                if(!isSearch) {
                    refresh("page=", String.valueOf(page), false);
                    page++;
                }
                else
                {
                    refresh("search_val=",edittext.getText().toString()+"&page="+String.valueOf(searchPage),false);
                    searchPage++;
                }
            }
        });

        //검색버튼 클릭리스너
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh("search_val=", edittext.getText().toString(), true);
                isSearch=true;
                searchPage=2;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;

                episodeString=new ArrayList<String>();
                episodeUrlString=new ArrayList<String>();
                progressBar.setVisibility(View.VISIBLE);
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


    }

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
                                Log.e("adapter trigger", "adapter trigger");
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
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error!","Text Parsing Error");
                progressBar.setVisibility(View.GONE);
            }
        });
        myRequestQueue.add(myRequest);

    }

    //화수 팝업
    void show()
    {
        final CharSequence[] items =  episodeString.toArray(new String[ episodeString.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        } else {
            // BackPressedForFinish 클래스의 onBackPressed() 함수를 호출한다.
            onBackPressedExit.onBackPressed();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
       // MenuInflater inflater=getMenuInflater();
        //inflater.inflate(R.menu.mainmenu,menu);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
