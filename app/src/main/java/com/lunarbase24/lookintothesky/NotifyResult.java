package com.lunarbase24.lookintothesky;

import java.util.GregorianCalendar;

/**
 * Created by Wada on 2016/10/07.
 * 通知結果データクラス
 * データメンバ
 * ウィジェットID
 * 時間帯フラグ
 * 更新日時
 */

public class NotifyResult {

    private int mWidgetID;
    private int mTimezone[] = {0, 0, 0, 0};
    private GregorianCalendar mUpdateTime;

    public NotifyResult(int id){
        mWidgetID = id;
    }

    public int getWidgetID(){
        return mWidgetID;
    }

    public void reset(){
        for(int i : mTimezone){
            i = 0;
        }
    }

}
