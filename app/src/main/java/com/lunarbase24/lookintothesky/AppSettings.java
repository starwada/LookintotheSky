package com.lunarbase24.lookintothesky;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Wada on 2016/07/29.
 * アプリ用設定データ
 * ウィジット用
 */
public class AppSettings {
    int m_nDataType;        // データ種別 0 PM2.5/1 OX
    int m_nDispHour;        // 表示時間 6～12時間
    int m_nUpdateTime;  // 更新時間 分
    int m_nTransp;        // グラフ背景透過率 0～255
    float m_fRadius;        // グラフ丸の半径

    Boolean m_bNotify;      // 通知フラグ
    int m_nNotifyValue;     // 通知設定値
    int m_nNotifyTimezone;  // 通知時間帯

    AppSettings(){
        onCreate();
    }

    public void onCreate() {
        m_nDataType = 0;
        m_nDispHour = 12;
        m_nUpdateTime = 15;
        m_nTransp = 128;
        m_fRadius = 6.0f;
        m_bNotify = false;
        m_nNotifyValue = 3;
        m_nNotifyTimezone = 6;
    }

    // 設定値を取得
    public void getPreference(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        m_nDispHour = Integer.parseInt(sharedPref.getString("disphour_preference", "11"));
        m_nUpdateTime = Integer.parseInt(sharedPref.getString("updatetime", "15"));
        m_nTransp = sharedPref.getInt("seekbar_transparency", 125);
        m_fRadius = (float)sharedPref.getInt("seekbar_dotradius", 8);
        m_bNotify = sharedPref.getBoolean("notify", false);
        m_nNotifyValue = Integer.parseInt(sharedPref.getString("colorlist_notify", "3"));
        m_nNotifyTimezone = Integer.parseInt(sharedPref.getString("timezonelist_notify", "6"));
    }

    // 各データのスピナー用インデックス
    // ウィジット 表示時間スピナー用
    public int DispHourIndex(){
        int nIndex = m_nDispHour-6;
        if(nIndex < 0 || nIndex > 6){ nIndex = 0; }

        return nIndex;
    }
    // ウィジット 更新時間スピナー用
    public int UpdateTimeIndex(){
        int nIndex = (int)(m_nUpdateTime/5);
        if(nIndex < 0 || nIndex >= 12){ nIndex = 0; }

        return nIndex;
    }

    // 比較
    public boolean isEqual(AppSettings settings){
        boolean flag = false;
        if(m_nDispHour == settings.m_nDispHour &&
        m_nUpdateTime == settings.m_nUpdateTime &&
        m_nTransp == settings.m_nTransp &&
        m_fRadius == settings.m_fRadius ){
            flag = true;
        }

        return flag;
    }

    //
    public void set(AppSettings settings){
        m_nDispHour = settings.m_nDispHour;
        m_nUpdateTime = settings.m_nUpdateTime;
        m_nTransp = settings.m_nTransp;
        m_fRadius = settings.m_fRadius;
    }
}
