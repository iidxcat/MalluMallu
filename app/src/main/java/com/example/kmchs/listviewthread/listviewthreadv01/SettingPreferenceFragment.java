package com.example.kmchs.listviewthread.listviewthreadv01;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

public class SettingPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_preference);
        Preference myPref=(Preference) findPreference("clearHistory");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences pref1;
                for(int i=1; i<33; i++) {
                    Context context=getActivity();
                    pref1 = context.getSharedPreferences("pref"+String.valueOf(i), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=pref1.edit();
                    editor.putString("title", null);
                    editor.commit();
                }
                Toast.makeText(getActivity(), "삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
