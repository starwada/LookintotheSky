package com.lunarbase24.lookintothesky;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

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
    private ArrayList<NotifyResult> mNotifyList = null;
    // 以下は設定画面の値
    private boolean mNotifyFlag;        // 通知フラグ
    private int mNotifyValueIndex;      // 通知しきい値
    private int mTimezone;              // 通知時間帯

    public NotifyGenerator(){
        mNotifyFlag = true;
        mNotifyValueIndex = 0;
        mTimezone = 0;
        if(mNotifyList == null){
            mNotifyList = new ArrayList<NotifyResult>();
        }
    }

    // 通知設定データ
    public void setNotifySettings(boolean flag, int index, int timezone){
        mNotifyFlag = flag;
        mNotifyValueIndex = index;
        mTimezone = timezone;
    }

    public void setWidgetInfo(int[] WidgetIds){
        for(int id: WidgetIds) {
            NotifyResult result = new NotifyResult(id);

            if (!mNotifyList.contains(result)) {
                mNotifyList.add(result);
            }
        }
    }

    private NotifyResult getResult(int appWidgetId){
        if(mNotifyList == null || mNotifyList.size() < 1){
            return null;
        }
        for(NotifyResult result : mNotifyList){
            if(result.getWidgetID() == appWidgetId){
                return result;
            }
        }
        return null;
    }

    public void notify(Context context, Soramame soramame, int appWidgetId, int type){
        // 当然通知不要時は未処理にて終了
        if(mNotifyFlag == false){
            return;
        }

        // 通知ジャッジ
        NotifyResult result = getResult(appWidgetId);
        if(result == null){
            return;
        }


        NotificationManager NotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_app_name)
                .setPriority(Notification.PRIORITY_DEFAULT)
                //.setCategory(Notification.CATEGORY_MESSAGE)
                .setContentTitle("Sample Notification")
                .setContentText("This is a normal notification.");

        Intent notifyintent = new Intent(context, SoraGraphActivity.class);
        notifyintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyintent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        int id=0;
        NotificationManager.notify(id, builder.build());

    }

    // 通知判定
    // 現在時間と通知時間帯とのカウント数をチェック
    // 閾値のチェック
    private boolean judge(NotifyResult result){
        boolean bOk = false;
        GregorianCalendar now = new GregorianCalendar(Locale.JAPAN);

        return bOk;
    }
}
