package com.lunarbase24.lookintothesky;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/*
    設定（セッティング）アクティビティ
    ウィジットの設定
    データ種別：PM2.5/OX
    表示時間：6～12時間
    更新時間：何分か（毎時）
    グラフ関連：背景の透過率？グラフ丸の半径
 */
public class SettingActivity extends PreferenceActivity {
    private static final String ACTION_CHANGE_SETTING = "com.lunarbase24.lookintothesky.ACTION_CHANGE_SETTING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 更新通知
        Intent alarmIntent = new Intent(SettingActivity.this, SoraAppWidget.class);
        alarmIntent.setAction(ACTION_CHANGE_SETTING);
        PendingIntent operation = PendingIntent.getBroadcast(SettingActivity.this, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager)SettingActivity.this.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, 0, operation);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setSummary("disphour_preference");
            setSummary("updatetime");
            updateView("colorlist_notify");
        }

        private SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                        setSummary(s);
                        updateView(s);
                    }
                };
//        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        // サマリー設定
        private void setSummary(String key){
            if(key.equals("disphour_preference")){
                Preference pref = findPreference(key);
                pref.setSummary(String.format("%s:%s",
                        getString(R.string.settings_summary_disphour), ((ListPreference)pref).getValue()));
            }
            else if(key.equals("updatetime")){
                Preference pref = findPreference(key);
                pref.setSummary(String.format("%s:%s %s",
                        getString(R.string.settings_summary_updatetime), ((ListPreference)pref).getValue(), getString(R.string.unit_minutes)));
            }
        }

        // View更新
        // これを実行すると、ColorListPreferenceに設定している色表示ビューまで更新される。
        private void updateView(String key) {
            if (key.equals("colorlist_notify")) {
                Preference pref = findPreference(key);
                pref.setSummary(String.format("%s\n%s",
                        getString(R.string.settings_summary_notifyvalue), ((ColorListPreference)pref).getValue()));
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }
    }
}
