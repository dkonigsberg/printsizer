package org.logicprobe.printsizer.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.logicprobe.printsizer.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}