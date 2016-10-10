package com.lunarbase24.lookintothesky;

import java.util.Calendar;
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

    public boolean checktimezone(GregorianCalendar now){
        boolean bOk = false;
        int index = now.get(Calendar.HOUR_OF_DAY)/6;
        if(index >= 0 && index < 4){
            if(mTimezone[index] == 1){
                bOk = true;
                mTimezone[index] = 0;
            }
        }
        return bOk;
    }

    public void reset(int timezone){
        int mask = 1;
        for(int i=0; i<4; i++) {
            mask = 1 << i;
            mTimezone[i] = 0;
            if((timezone & mask) != 0){
                mTimezone[i] = 1;
            }
        }
    }

}
