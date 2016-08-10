package com.lunarbase24.lookintothesky;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Wada on 2016/07/29.
 * アプリ用設定データ
 * ウィジット用
 */
public class AppSettings extends Application{
    int m_nDataType;        // データ種別 0 PM2.5/1 OX
    int m_nDispHour;        // 表示時間 6～12時間
    int m_nUpdateTime;  // 更新時間 分
    int m_nTransp;        // グラフ背景透過率 0～255
    float m_fRadius;        // グラフ丸の半径

    AppSettings(){
        onCreate();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m_nDataType = 0;
        m_nDispHour = 12;
        m_nUpdateTime = 15;
        m_nTransp = 128;
        m_fRadius = 6.0f;
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
