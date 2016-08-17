package com.lunarbase24.lookintothesky;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.os.Bundle;

/*
    設定（セッティング）アクティビティ
    ウィジットの設定
    データ種別：PM2.5/OX
    表示時間：6～12時間
    更新時間：何分か（毎時）
    グラフ関連：背景の透過率？グラフ丸の半径
 */
public class SettingActivity extends Activity {
    private AppSettings m_settings;
    private AppSettings update;
    private static final String ACTION_CHANGE_SETTING = "com.lunarbase24.lookintothesky.ACTION_CHANGE_SETTING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
