package com.travelersdiary.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;

import com.travelersdiary.R;
import com.travelersdiary.services.SyncService;

public class PreferencesFragment extends PreferenceFragment {

    private CheckBoxPreference enable_sync_service_checkBox;
    private ListPreference sync_interval_list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        enable_sync_service_checkBox = (CheckBoxPreference) findPreference("sync_service_check_box");
        sync_interval_list = (ListPreference) findPreference("sync_interval");

        sync_interval_list.setEnabled(enable_sync_service_checkBox.isChecked());

        enable_sync_service_checkBox.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                sync_interval_list.setEnabled(enable_sync_service_checkBox.isChecked());
                return false;
            }
        });

        enable_sync_service_checkBox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = (Boolean) newValue;
                if (enabled) {
                    getActivity().startService(new Intent(getActivity(), SyncService.class));
                } else {
                    getActivity().stopService(new Intent(getActivity(), SyncService.class));
                }
                return true;
            }
        });

        sync_interval_list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getActivity().stopService(new Intent(getActivity(), SyncService.class));
                getActivity().startService(new Intent(getActivity(), SyncService.class));
                return true;
            }
        });
    }

}
