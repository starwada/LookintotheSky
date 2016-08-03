package com.lunarbase24.lookintothesky;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SoraAppWidgetConfigureActivity SoraAppWidgetConfigureActivity}
 * 測定結果のウィジットなので、１時間毎に更新させる。
 * タイマーを使用してみたが、うまく動作しなかった。途中でタイマーが止まってしまった（使い方が悪いのだろうが）。
 * タイマーのコードは残しておく。
 * 端末が再起動した際に、ウィジットの表示がおかしくなっていた（というか、基本の画像にTextViewが張り付いているだけ）。
 * つまり、再起動時に当然、ウィジットも更新しないといけない。onUpdate()が呼ばれる。
 */
public class SoraAppWidget extends AppWidgetProvider {
//    private static Timer timer;
    private static int nCount=0;
    private static final String ACTION_START = "com.lunarbase24.lookintothesky.ACTION_START";
    private static final String ACTION_START_MY_ALARM = "com.lunarbase24.lookintothesky.ACTION_START_MY_ALARM";
    private static final String ACTION_START_SHARE = "com.lunarbase24.lookintothesky.ACTION_START_SHARE";
    private static final String ACTION_CHANGE_SETTING = "com.lunarbase24.lookintothesky.ACTION_CHANGE_SETTING";
    private final long interval = 60 * 60 * 1000;
    private long alarmtime = 30 * 60 * 1000;  // アラーム設定分

    // 表示区分 PM2.5 OX（光化学オキシダント） WS（風速）
    // GraphFactoryにも同様に定義している
    private static final float mDotY[][] = { {10.0f, 15.0f, 35.0f, 50.0f, 70.0f, 100.0f },
            {0.02f, 0.04f, 0.06f, 0.12f, 0.24f, 0.34f },
            {4.0f, 7.0f, 10.0f, 13.0f, 15.0f, 25.0f}};

    // 以下はシステムのタイミングで呼ばれる
    // 最初、ウィジットを画面に配置する際に設定アクティビティよりも先に呼ばれる。
    // 後は、システムのタイミング
    // 再起動時にも呼ばれる。
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
//        final int N = appWidgetIds.length;
//        for (int i = 0; i < N; i++) {
//            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
//        }
    }

    @Override
    // ウィジットが削除される度に呼ばれる。
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            SoraAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
            File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                    String.format("/soracapture_%d_%d.png", appWidgetIds[i], SoramameAccessor.getWidgetID(context, appWidgetIds[i])));
            if(image.exists()) {
                image.delete();
            }
            SoramameAccessor.deleteWidgetID(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        // 初回に１度だけタイマー起動
//        if(timer == null) {
//            timer = new Timer();
//            timer.schedule(createTimerTask(context), 0, 1000 * 60);
//        }
    }

    @Override
    // 最後のウィジットが削除されるタイミングで呼ばれる。
    // onDeleted()より後。
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        // ウィジットが無くなたらタイマーキャンセル
//        if(timer != null){
//            nCount = 0;
//            timer.cancel();
//            timer = null;
//        }
    }

    // ウィジット更新
    // Context context
    // AppWidgetManager appWidgetManager
    // Soramame soramame    計測データ
    // int appWidgetId  ウィジットID
    // int type データ種別 0 PM2.5/1 OX
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                Soramame soramame, int appWidgetId, int type) {

        // ウィジット設定アクティビティ（画面）にて設定した文字列（Prefファイルに保持）をここで取得。
//        CharSequence widgetText = SoraAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId) + String.format("%d", nCount);
        // Soramameを渡すようにしたので、以下はいらないな。
        int nCode = SoraAppWidgetConfigureActivity.loadPref(context, appWidgetId);
        if( nCode == 0 ){ return ; }
        // Construct the RemoteViews object
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sora_app_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        RemoteViews image = new RemoteViews(context.getPackageName(), R.layout.sora_app_widget);
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inJustDecodeBounds = true;
        // optionsの設定を間違うと、以下の関数ではBitmapが作成されない。
        //Bitmap bmap = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/capture.jpeg", options);
        // とりあえず、optionsは未設定（規定値）の以下にて表示されるようになった。
        Bitmap bmap = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                String.format("/soracapture_%d_%d.png", appWidgetId, type));
        image.setImageViewBitmap(R.id.appwidget_image, bmap);
        // 計測値表示
        // 表示データ種別および値にて色を設定
        CharSequence widgetText = null;
        int nColor = 0;
        int nTypeface = Typeface.ITALIC;
        float fSize = 48.0f;
        boolean flag = true;
        switch(type){
            case 0:
                widgetText = soramame.getData().get(0).getPM25String();
                nColor = soramame.getColor(Soramame.SORAMAME_MODE_PM25, 0);
                flag = soramame.getData().get(0).isValidatePM25();
                break;
            case 1:
                fSize = 40.0f;
                widgetText = soramame.getData().get(0).getOXString();
                nColor = soramame.getColor(Soramame.SORAMAME_MODE_OX, 0);
                flag = soramame.getData().get(0).isValidateOX();
                break;
        }
        // 未計測の場合は文字を小さくする
        if(!flag) {
            fSize = 28.0f;
            nTypeface = Typeface.NORMAL;
        }

        // 計測値にスタイルを適用したいため、以下を使用。
        // "未計測"をイタリックにすると、wrap_contentにて端が表示されないので、スタイルを変更する。
        // 計測値はイタリックが見栄えがいいので。
        SpannableString text = new SpannableString(widgetText);
        text.setSpan(new StyleSpan(nTypeface), 0, text.length(), 0);

        image.setTextColor(R.id.appwidget_text, nColor);
        image.setTextViewText(R.id.appwidget_text, text);
        image.setTextViewTextSize(R.id.appwidget_text, TypedValue.COMPLEX_UNIT_DIP, fSize);
        image.setImageViewResource(R.id.shareButton, R.drawable.ic_share);
        image.setImageViewResource(R.id.updateButton, R.drawable.ic_update);

        // ここは、テキストをクリックしたらMainActivityが起動する仕組みを設定している。
        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        image.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
        // 共有（トーストを表示するだけ、今のところ）
        Intent shareIntent = new Intent(context, SoraAppWidget.class);
        shareIntent.setAction(ACTION_START_SHARE);
        PendingIntent operation = PendingIntent.getBroadcast(context, appWidgetId, shareIntent, 0);
        image.setOnClickPendingIntent(R.id.shareButton, operation);
        // 更新
        Intent updateIntent = new Intent(context, SoraAppWidget.class);
        updateIntent.setAction(ACTION_START_MY_ALARM);
        PendingIntent update = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, 0);
        image.setOnClickPendingIntent(R.id.updateButton, update);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, image);
    }

    // 最初にここが呼ばれる。
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // ホーム画面が回転されたのをキャッチする
        // 以下で回転の状態を取得する。
        //context.getResources().getConfiguration().orientation
        // 回転の状態（縦、横）に応じてウィジットの配置設定を修正する
        // アラーム受信 更新処理（onUpdateでなくここで処理をする、アラーム処理と内容が同じなので）
        if (intent.getAction().equals(ACTION_START_MY_ALARM) ||
                intent.getAction().equals(ACTION_START) ||
                intent.getAction().equals(ACTION_CHANGE_SETTING) ||
                intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
// Alarmのサンプルにしたのが以下のコードを書いていた。意味があるのか不明なのでコメント化
//            if (ACTION_START_MY_ALARM.equals(intent.getAction())) {
            Toast toast = Toast.makeText(context, String.format("%s", intent.getAction()), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.START, 0, 0);
            toast.show();

            AppSettings settings = (AppSettings)context.getApplicationContext();
            if(settings == null){ return; }
            alarmtime = settings.m_nUpdateTime * 60 * 1000;
            // 初回配置時にIDとデータ種別を保持
            if (intent.getAction().equals(ACTION_START) ){
                SoramameAccessor.setWidgetID(context, intent.getIntExtra("WidgetID", 0), intent.getIntExtra("DataType", 0));
            }
            Intent serviceIntent = new Intent(context, MyService.class);
            context.startService(serviceIntent);
//            }
            setAlarm(context);
        }
        // 共有受信
        if (intent.getAction().equals(ACTION_START_SHARE)) {
            Toast toast = Toast.makeText(context, "Share", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.START, 0, 0);
            toast.show();
        }
    }

    // アラーム設定（これをその都度呼び出している）
    private void setAlarm(Context context) {
        Intent alarmIntent = new Intent(context, SoraAppWidget.class);
        alarmIntent.setAction(ACTION_START_MY_ALARM);
        PendingIntent operation = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        long now = System.currentTimeMillis() + 1; // + 1 は確実に未来時刻になるようにする保険
        // 以下は毎正時にアラーム
        long oneHourAfter = now + interval - now % (interval);
        // 毎指定分(alarmtime)にアラーム
        long lCurrent = now % interval;
        if( Math.abs(lCurrent - alarmtime) < 1000*60 ){ oneHourAfter = now + interval; }
        else if(lCurrent < alarmtime){ oneHourAfter = now - lCurrent + alarmtime; }
        else{ oneHourAfter = now - lCurrent + alarmtime + interval; }
        am.set(AlarmManager.RTC, oneHourAfter, operation);
    }

    public static class MyService extends Service {
        AppSettings mSettings;
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            try {
                mSettings = (AppSettings) this.getApplication();
                ComponentName thisWidget = new ComponentName(this, SoraAppWidget.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(this);
                int appWidgetIds[] = manager.getAppWidgetIds(thisWidget);

                // デバッグ用コード 呼ばれるタイミングを出力
//                Date now = new Date();
//                // MODE_APPENDにて既存ファイルの場合追加
//                FileOutputStream outfile = openFileOutput(SORADATEFILE, Context.MODE_APPEND);
//                outfile.write(String.format( Locale.ENGLISH, "%s flag:%d startId:%d\n", now.toString(), flags, startId).getBytes());
//                outfile.close();

                final int N = appWidgetIds.length;
                for (int i = 0; i < N; i++) {
                    new SoraDesc().execute(appWidgetIds[i], SoramameAccessor.getWidgetID(this, appWidgetIds[i]));
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            return 0;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private class SoraDesc extends AsyncTask<Integer, Void, Integer>
        {
            int count = 0;
            int appWidgetId = 0;
            int nType = 0;
            Soramame soramame = null;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(Integer... appWidgetIds)
            {
                int rc = 0;
                try
                {
                    String strMstURL = "";
                    appWidgetId = appWidgetIds[0];
                    nType = appWidgetIds[1];
                    // 測定局コード取得
                    int nCode = SoraAppWidgetConfigureActivity.loadPref(MyService.this, appWidgetId);
                    if(nCode == 0){ return -1; }

                    // getStation()の戻り値はデータ数
                    String[] station = new String[2] ;
                    if( SoramameAccessor.getStation(MyService.this, nCode, station) < 1 ){ return -2; }
                    soramame = new Soramame(nCode, station[0], station[1]);

                    ArrayList<Soramame> list = new ArrayList<Soramame>();
                    list.add(soramame);

                    rc = SoramameAccessor.getSoramameData(MyService.this, list);
                }
                catch(Exception e)
                {
                    rc = -3;
                    e.printStackTrace();
                }
                return rc;
            }

            @Override
            protected void onPostExecute(Integer result)
            {
                if(result < 0){ return ; }
                if(soramame.getData() == null){
                    Toast toast = Toast.makeText(MyService.this, String.format("%s データなし", soramame.getMstName()), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.START, 0, 0);
                    toast.show();
                    return;
                }

                // こうすると、更新する度に新しい設定で作成される。
                // 表示時間と更新時間はいいけど、データ種別が変わってしまう。
                // ウィジット毎に設定を・・・
                // つまり、データ種別はウィジット作成時に決定する。
                AppSettings settings = (AppSettings)MyService.this.getApplication();
                Bitmap graph = GraphFactory.drawGraph(soramame, appWidgetId, nType, mSettings);
                // ここでウィジット更新
                AppWidgetManager manager = AppWidgetManager.getInstance(MyService.this);
                updateAppWidget(MyService.this, manager, soramame, appWidgetId, nType);
            }
        }
    }

    // 以下はタイマー用のコード。未使用だがコメント化しておく。
//    private TimerTask createTimerTask(final Context context) {
//
//        return new TimerTask() {
//            @Override
//            public void run() {
//                Message message = new Message();
//                createHandler(context).sendMessage(message);
//            }
//        };
//    }
//
//     // Handler
//    private Handler createHandler(final Context context) {
//        return new Handler(Looper.getMainLooper()) {
//            public void handleMessage(Message msg) {
//
//                updateWidget(context);
//            }
//        };
//    }
//
//    // ウィジット更新
//    private void updateWidget(Context context) {
//        ComponentName thisWidget = new ComponentName(context, SoraAppWidget.class);
//        AppWidgetManager manager = AppWidgetManager.getInstance(context);
//        int appWidgetIds[] = manager.getAppWidgetIds(thisWidget);
//
//        nCount++;
//        onUpdate(context, manager, appWidgetIds);
//    }

}

