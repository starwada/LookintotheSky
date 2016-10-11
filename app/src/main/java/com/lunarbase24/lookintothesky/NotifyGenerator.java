package com.lunarbase24.lookintothesky;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.lunarbase24.lookintothesky.R.mipmap.icon_lookintothesky;

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
    private int mNotifyValueIndex;      // 通知しきい値インデックス
    private int mTimezone;              // 通知時間帯

    public NotifyGenerator(){
        mNotifyFlag = true;
        mNotifyValueIndex = 0;
        mTimezone = 0;
        if(mNotifyList == null){
            mNotifyList = new ArrayList<NotifyResult>();
        }
    }

    // 通知情報設定関数
    // flag:通知フラグ
    // index:通知閾値
    // timezone:通知時間帯
    // WidgetIds:ウィジェット番号
    public void setNotify(boolean flag, int index, int timezone, int[] WidgetIds){
        // 通知条件設定
        setNotifySettings(flag, index, timezone);
        // 対象ウィジェット設定
        setWidgetInfo(WidgetIds);
        //
        update();
    }

    // 通知設定データ
    private void setNotifySettings(boolean flag, int index, int timezone){
        mNotifyFlag = flag;
        mNotifyValueIndex = index;
        mTimezone = timezone;
    }

    // 通知対象ウィジェット設定
    // WIdgetIds:ウィジェット番号
    private void setWidgetInfo(int[] WidgetIds){
        for(int id: WidgetIds) {
            NotifyResult result = new NotifyResult(id);

            if (!mNotifyList.contains(result)) {
                result.setTimezone(mTimezone);
                mNotifyList.add(result);
            }
        }
    }

    // 通知情報更新
    // 通知時間帯は1日内での設定で、日付が変わると再度初期化する必要がある。
    // よって、現在時間より日付が変わったと判断されると、通知情報（NotifyResult）の
    // 通知時間帯データを初期化する。
    private void update(){

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

    // 通知処理
    // context:コンテキスト
    // soramame:計測データ
    // appWidgetId:対象ウィジェット
    // type:データ種別
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
        if(!judge(result, soramame, type)){
            return;
        }

        String msg = String.format("%s\n%s\n%s",
                soramame.getMstName(), soramame.getData(type, 0), context.getString(R.string.notify_message));
        // 通知発生
        NotificationManager NotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_app_name)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentTitle(context.getString(R.string.notify_title))
                .setContentText(msg)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        Intent notifyintent = new Intent(context, SoraGraphActivity.class);
        notifyintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyintent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        int id=0;
        NotificationManager.notify(id, builder.build());

    }

    // 通知判定
    // result:通知結果情報
    // data:計測データ
    // type:データタイプ 0 PM2.5/1
    // 現在時間と通知時間帯とのカウント数をチェック
    // 閾値のチェック
    private boolean judge(NotifyResult result, Soramame data, int type){
        boolean bOk = false;
        // 通知時間帯チェック
        GregorianCalendar now = new GregorianCalendar(Locale.JAPAN);

        bOk = result.checktimezone(now);
        if(!bOk){
            return bOk;
        }

        int mode = 0;
        switch(type){
            case 0:
                mode = Soramame.SORAMAME_MODE_PM25;
                break;
            case 1:
                mode = Soramame.SORAMAME_MODE_OX;
                break;
        }
        // 閾値チェック
        if(mNotifyValueIndex <= data.getColorIndex(mode, 0)){
            bOk = true;
        }

        return bOk;
    }
}
