package com.geekbrains.city_weather.preferences;

import android.os.Bundle;

import com.geekbrains.city_weather.R;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_setting, rootKey);
    }
}
