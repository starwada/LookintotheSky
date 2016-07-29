package com.lunarbase24.lookintothesky;

import android.app.Application;

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

    @Override
    public void onCreate() {
        super.onCreate();
        m_nDataType = 0;
        m_nDispHour = 12;
        m_nUpdateTime = 15;
        m_nTransp = 128;
        m_fRadius = 8.0f;
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
}
