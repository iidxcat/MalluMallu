package com.example.kmchs.listviewthread.listviewthreadv01;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class PrefManager {
    Context context;

    public PrefManager(Context context) {
        this.context=context;
    }
    public void addData(String thumbnailUrl, String title, String date, String url)
    {
        switchData();
        SharedPreferences pref1 =context.getSharedPreferences("pref1",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=pref1.edit();
        editor.putString("thumbnailUrl",thumbnailUrl);
        editor.putString("title",title);
        editor.putString("date",date);
        editor.putString("url",url);
        editor.commit();
    }
    public void addSingleData(String title, String date)
    {
        SharedPreferences pref1 =context.getSharedPreferences("pref1",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=pref1.edit();
        editor.putString("title",title);
        editor.putString("date",date);
        editor.commit();
    }
    private void switchData()
    {
        SharedPreferences pref1,pref2;
        for(int num=31; num>0; num--) {
            pref1 = context.getSharedPreferences("pref" + String.valueOf(num), Context.MODE_PRIVATE);
            pref2 = context.getSharedPreferences("pref" + String.valueOf(num+1), Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = pref2.edit();
            editor.putString("thumbnailUrl", pref1.getString("thumbnailUrl", null));
            editor.putString("title", pref1.getString("title", null));
            editor.putString("date", pref1.getString("date", null));
            editor.putString("url", pref1.getString("url", null));
            editor.commit();
        }
    }
}
