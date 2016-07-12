package com.android.voicenote.settings;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.voicenote.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFrag extends PreferenceFragment {

    SettingCallBack mCallbacks;

    public interface SettingCallBack {
        void onNameChanged(String newName);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof SettingCallBack)) {
            throw new IllegalStateException("MainActivity未实现回调接口");
        }
        mCallbacks = (SettingCallBack) activity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListPreference alarmWay = (ListPreference) getPreferenceManager().findPreference("set_alarm_way");
        ListPreference language = (ListPreference) getPreferenceManager().findPreference("set_speak_language");
        EditTextPreference userName = (EditTextPreference)getPreferenceManager().findPreference("set_user_name");
        alarmWay.setOnPreferenceChangeListener(preferenceChangeListener);
        language.setOnPreferenceChangeListener(preferenceChangeListener);
        userName.setOnPreferenceChangeListener(preferenceChangeListener);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private Preference.OnPreferenceChangeListener preferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            SharedPreferences preferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            switch (key){
                case "set_user_name":
                    editor.putString("name", newValue.toString());
                    editor.apply();
                    mCallbacks.onNameChanged(newValue.toString());
                    break;
                case "set_alarm_way":
                    editor.putInt("remind", ((ListPreference)preference).findIndexOfValue(newValue.toString()));
                    editor.apply();
                    break;
                case "set_speak_language":
                    editor.putInt("language", ((ListPreference)preference).findIndexOfValue(newValue.toString()));
                    editor.apply();
                    break;
            }
            return true;
        }
    };
}
