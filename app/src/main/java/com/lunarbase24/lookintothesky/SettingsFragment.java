package com.lunarbase24.lookintothesky;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Wada on 2016/08/17.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
