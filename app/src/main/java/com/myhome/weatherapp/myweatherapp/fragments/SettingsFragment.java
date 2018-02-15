package com.myhome.weatherapp.myweatherapp.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myhome.weatherapp.myweatherapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        Preference.OnPreferenceChangeListener{

    final static String TAG = "DEBUG(SettingsFrag):";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_preferences);

        // setup onchangelistener for the units listpreference
        Preference pref = findPreference(getString(R.string.pref_units_key));
        pref.setOnPreferenceChangeListener(this);
        pref.setSummary(((ListPreference)pref).getValue());
        Log.d(TAG,"onCreatePreferences(): setting up initial preference summary for units");
    }

    /**
     * This callback is done before the preference change is saved. Therefore, use newValue value
     * only and dont try to .getValue() from the preference as the value has not been set yet.
     * @param preference
     * @param newValue
     * @return
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG,"onPreferenceChange(): preference change detected");

        if (preference.getKey().equals(getString(R.string.pref_units_key))){
            // the units preference was changed
            ListPreference lPref = (ListPreference)preference;
            lPref.setSummary((String)newValue);
            Log.d(TAG,"onPreferenceChange(): Units preference change detected.New value="+lPref.getSummary());
        }
        return true;
    }
}
