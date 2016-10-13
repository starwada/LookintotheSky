package com.lunarbase24.lookintothesky;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Locale;

public class SoraGraphActivity extends AppCompatActivity {
    ArrayList<Soramame> mList = null;
    int mGraphTransparency = 0;   // グラフの背景透過率

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sora_graph);

        updateGraph();
    }

    private void updateGraph(){
        Intent intent = getIntent();
        int code = intent.getIntExtra("stationcode", 0);
        int type = intent.getIntExtra("type", 1);

        // DBから指定測定局コードのデータを抽出。
        String station[] = new String[2];
        if(SoramameAccessor.getStation(this, code, station) < 1){
            return;
        }

        Soramame data = new Soramame(code, station[0], station[1]);
        if(mList == null) {
            mList = new ArrayList<Soramame>();
        }
        mList.clear();
        mList.add(data);

        // グラフ背景透過率取得
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mGraphTransparency = sharedPref.getInt("graph_transparency", 100);

        new SoraTask().execute(type);
    }

    public void setShareIntent(String strText) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setPackage("com.twitter.android");
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/capture.jpeg"));
        shareIntent.putExtra(Intent.EXTRA_TEXT, strText);
        shareIntent.setType("text/plain");
        startActivity(shareIntent);
    }

    private class SoraTask extends AsyncTask<Integer, Void, Void> {
        ProgressDialog mProgressDialog;
        int count = 0;
        Integer mType;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(SoraGraphActivity.this);
            mProgressDialog.setTitle("そらまめ データ取得");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                mType = params[0];
                SoramameAccessor.getSoramameData(SoraGraphActivity.this, mList, 120);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            // 最新のデータを表示する。
            Soramame data = mList.get(0);
            final SoraGraphView sora = (SoraGraphView) findViewById(R.id.soragraph);
            sora.setData(data);
            sora.setMode(mType);
            sora.setTransparency(mGraphTransparency);
            sora.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getActionMasked() == MotionEvent.ACTION_MOVE ) {
                        float px = event.getX(0);
                        int sum = event.getPointerCount();
                        sora.Touch(px);
                    }
                    // ACTION_DOWNしか通知されないので苦肉の策。
                    else if(event.getActionMasked() == MotionEvent.ACTION_UP ||
                            event.getActionMasked() == MotionEvent.ACTION_DOWN){
                        float px = event.getX(0);
                        int sum = event.getPointerCount();
                        sora.Touch(px);
                        TouchGraph(sora);
                    }
                    return false;
                }
            });
            TouchGraph(sora);

            TextView station = (TextView)findViewById(R.id.MstName);
            station.setText(data.getMstName());
            station.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Soramame data = mList.get(0);
                    Uri location = Uri.parse("geo:0,0?q=" + Uri.encode(data.getAddress()));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
                    mapIntent.setPackage("com.google.android.apps.maps");

                    if (mapIntent.resolveActivity(SoraGraphActivity.this.getPackageManager()) != null) {
                        SoraGraphActivity.this.startActivity(mapIntent);
                    }
                    return false;
                }
            });

            TextView max = (TextView)findViewById(R.id.soramax);
            max.setText(sora.getMaxString());
            TextView ave = (TextView)findViewById(R.id.soraave);
            ave.setText(sora.getAveString());

            ImageView capture = (ImageView)findViewById(R.id.card_snap);
            capture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    sora.Capture();
                    sora.showToast();
                    return false;
                }
            });

            mProgressDialog.dismiss();
        }

        // グラフタッチ処理
        private void TouchGraph(SoraGraphView sora){
            Soramame.SoramameData val = sora.getPosData();
            TextView date = (TextView)findViewById(R.id.date);
            date.setText(val.getDateString());
            TextView hour = (TextView)findViewById(R.id.hour);
            hour.setText(val.getHourString());
            TextView value = (TextView)findViewById(R.id.value);
            ImageView ws = (ImageView)findViewById(R.id.imageWS);
            switch(mType){
                case Soramame.SORAMAME_MODE_OX:
                    value.setText(val.getOXString());
                    ws.setVisibility(View.INVISIBLE);
                    break;
                case Soramame.SORAMAME_MODE_PM25:
                    value.setText(val.getPM25String());
                    ws.setVisibility(View.INVISIBLE);
                    break;
                case Soramame.SORAMAME_MODE_WS:
                    value.setText(val.getWSString()+"  ");
                    if(val.getWDRotation() < 0.0f) {
                        ws.setVisibility(View.INVISIBLE);
                    }
                    else{
                        ws.setVisibility(View.VISIBLE);
                    }
                    ws.setRotation(val.getWDRotation());
                    break;
            }
        }
    }
}
