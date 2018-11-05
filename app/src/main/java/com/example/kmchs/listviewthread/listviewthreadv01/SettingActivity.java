package com.example.kmchs.listviewthread.listviewthreadv01;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sharedPref.getBoolean("blackTheme",false))
            setContentView(R.layout.activity_setting);
        else {
            setContentView(R.layout.activity_setting);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkPrimary)));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkSecondary));
        }
        //setContentView(R.layout.activity_setting);
    }
}
