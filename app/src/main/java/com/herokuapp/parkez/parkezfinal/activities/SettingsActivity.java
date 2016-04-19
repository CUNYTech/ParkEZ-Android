package com.herokuapp.parkez.parkezfinal.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.herokuapp.parkez.parkezfinal.R;

/**
 * Created by xsang on 4/19/2016.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    // @SuppressLint("ValidFragment")
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d("settings", "addPreferences");
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
