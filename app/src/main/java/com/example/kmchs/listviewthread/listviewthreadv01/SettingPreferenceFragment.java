package com.example.kmchs.listviewthread.listviewthreadv01;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_preference);
    }
}
