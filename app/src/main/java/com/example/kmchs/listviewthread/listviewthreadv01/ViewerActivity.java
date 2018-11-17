package com.example.kmchs.listviewthread.listviewthreadv01;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class ViewerActivity extends AppCompatActivity {
    //ImageView[] images=new ImageView[100];
    boolean isBreak=false;
    int pos;
    private WebView webView;
    private WebSettings webSettings;
    Toolbar toolbar;
    String title;

    Boolean isZoomEnabled=false;
    Boolean isNextExist=false;
    Boolean isPreviousExist=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if(!sharedPref.getBoolean("blackTheme",false)) {
            setContentView(R.layout.activity_viewer);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        else {
            setContentView(R.layout.viewer_dark);
            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkPrimary)));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkSecondary));
        }

        toolbar = (Toolbar) findViewById(R.id.viewerToolbar);
        setSupportActionBar(toolbar);

        isZoomEnabled= sharedPref.getBoolean("zoom",false);

        webView=(WebView)findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webSettings=webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if(isZoomEnabled) {
            webSettings.setBuiltInZoomControls(true);
            webSettings.setSupportZoom(true);
        }


       Intent intent=getIntent();
       pos=intent.getIntExtra("pos",0);
       title=intent.getStringExtra("Title");
       int titleLength=title.length();
       if(titleLength>18)
       {
           StringBuilder builder=new StringBuilder(title);
           builder.delete(11,titleLength-6);
           builder.insert(11,"...");
           title=builder.toString();
       }
        getSupportActionBar().setTitle(title);
        StringBuilder builder=new StringBuilder(intent.getStringExtra("ID"));
        builder.delete(0,2);
        String url=builder.toString();
        isNextExist=intent.getBooleanExtra("next",true);
        isPreviousExist=intent.getBooleanExtra("previous",true);

        registerForContextMenu(webView);
        webView.loadUrl("https://aqours.faith/call_frame?&o="+url+"&what=list");


    }
    public void onBackPressed()
    {
        Intent resultIntent=new Intent();
        resultIntent.putExtra("data",0);
        setResult(RESULT_OK,resultIntent);
        finish();
    }

   @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //MenuInflater inflater=getMenuInflater();
        //inflater.inflate(R.menu.viewermenu,menu);
        getMenuInflater().inflate(R.menu.viewermenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId()) {
            case R.id.next:
                Intent resultIntent=new Intent();
                // 0=종료,1=다음,2=이전
                if(!isNextExist)
                    Toast.makeText(ViewerActivity.this, "최신 화 입니다.", Toast.LENGTH_SHORT).show();
                else {
                    resultIntent.putExtra("data", 1);
                    resultIntent.putExtra("pos",pos);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
                return true;
            case R.id.previous:
                Intent resultIntent2=new Intent();
                if(!isPreviousExist)
                    Toast.makeText(ViewerActivity.this, "처음 화 입니다.", Toast.LENGTH_SHORT).show();
                else {
                    resultIntent2.putExtra("data", 2);
                    resultIntent2.putExtra("pos",pos);
                    setResult(RESULT_OK, resultIntent2);
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo contextMenuInfo){
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);

        final WebView.HitTestResult webViewHitTestResult = webView.getHitTestResult();

        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            contextMenu.setHeaderTitle("이미지 저장");

            contextMenu.add(0, 1, 0, "저장")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            String DownloadImageURL = webViewHitTestResult.getExtra();
                            StringBuilder builder=new StringBuilder(DownloadImageURL);
                            //builder.replace("https://lovelive.aqours.faith/"," ");
                            if(URLUtil.isValidUrl(DownloadImageURL)){

                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                                request.allowScanningByMediaScanner();
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,title+DownloadImageURL);

                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);

                                Toast.makeText(ViewerActivity.this,"다운로드 시작..",Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(ViewerActivity.this,"Error",Toast.LENGTH_LONG).show();
                            }
                            return false;
                        }
                    });
        }
    }

}
