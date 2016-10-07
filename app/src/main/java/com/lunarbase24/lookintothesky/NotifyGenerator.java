package com.lunarbase24.lookintothesky;

import java.util.ArrayList;

/**
 * Created by Wada on 2016/10/07.
 * 通知生成クラス
 * static 関数
 * 通知更新処理
 * 通知発行処理
 * メンバ関数
 * 通知判定
 */

public class NotifyGenerator {
    private ArrayList<NotifyResult> mNotifyList;
    private boolean mNotifyFlag;
    private int mNotifyValueIndex;
    private int mTimezone;

    // 通知設定データ
    public void setNotify(boolean flag, int index, int timezone){
        mNotifyFlag = flag;
        mNotifyValueIndex = index;
        mTimezone = timezone;
    }
}
