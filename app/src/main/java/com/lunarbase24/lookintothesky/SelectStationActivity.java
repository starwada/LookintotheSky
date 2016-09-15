package com.lunarbase24.lookintothesky;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by Wada on 2016/05/31
 * CardView,RecyclerView用に書き直し
 * 測定局選択画面
 * メイン画面の測定局毎グラフ用に表示する測定局を選択する画面
 * 前回指定した都道府県を保持しておく
 * 都道府県の測定局を取得する（どの時点で更新されるか分からないので）
 * メイン画面で表示する測定局（コード）はファイルに保持しておく
 */
public class SelectStationActivity extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private static final String SORAPREFFILE = "SoraPrefFile";

    private static  final  String SORABASEURL="http://soramame.taiki.go.jp/";
    private static final String SORASUBURL ="MstItiran.php";
    private static final String SORADATAURL = "DataList.php?MstCode=";
    // 指定都道府県の測定局一覧取得
    private static final String SORAPREFURL ="MstItiranFrame.php?Pref=";

    ArrayList<Soramame> mList;
    int mPref ;                     // 都道府県コード

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectstation);

        try {
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(myToolbar);
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setIcon(R.drawable.ic_app_name);
            // Get a support ActionBar corresponding to this toolbar
            ActionBar ab = getSupportActionBar();
            // Enable the Up button
            ab.setDisplayHomeAsUpEnabled(true);

            mPref = 0;
            // 都道府県インデックスを取得
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            mPref = sharedPref.getInt("CurrentPref", 1);

            // SORASUBURLから都道府県名とコードを取得、スピナーに設定
            Spinner prefspinner = (Spinner) findViewById(R.id.spinner);
            if(prefspinner != null) {
                prefspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        mPref = position + 1;

                        // 選択都道府県での測定局データを取得するまえに、
                        // 現在の選択状態をDBに保存
                        setSelectedStation();

                        // 選択都道府県での測定局データ取得
                        new SoraStation().execute(false);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            // 都道府県取得
            new PrefSpinner().execute();

            //SwipeRefreshLayoutとListenerの設定
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
            }

        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    //swipeでリフレッシュした時の通信処理とグルグルを止める設定を書く
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            /*リフレッシュした時の通信処理を書く*/
            // DBから削除した後に、再度取得する。
            new SoraStation().execute(true);

            //setRefreshing(false)でグルグル終了できる
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    // 測定局の選択フラグをDBに設定する
    private int setSelectedStation(){
        int rc = 0;
        if( mList != null ){
            SoramameSQLHelper mDbHelper = new SoramameSQLHelper(SelectStationActivity.this);
            try {
                int nCount[] = {0,0};
                for(Soramame data : mList){
                    if(data.isEdit()){
                        if(!data.isSelected()){ nCount[0]++; }
                        else{ nCount[1]++; }
                    }
                }

                SQLiteDatabase mDb = mDbHelper.getWritableDatabase();
                String strWhereCause;
                ContentValues values = new ContentValues();
                for( int i=0; i<2; i++) {
                    strWhereCause = SoramameContract.FeedEntry.COLUMN_NAME_CODE + " IN(";
                    values.put(SoramameContract.FeedEntry.COLUMN_NAME_SEL, i);
                    if(nCount[i] > 0) {
                        String strWhereArg[] = new String[nCount[i]];
                        int l=0;
                        for (Soramame data : mList) {
                            if (data.isEdit()) {
                                if ((i == 0 && !data.isSelected()) ||
                                        (i == 1 && data.isSelected())) {
                                    strWhereArg[l++] = String.valueOf(data.getMstCode());
                                    strWhereCause += "?";
                                    if(l < nCount[i]){ strWhereCause += ","; }
                                }
                            }
                        }
                        strWhereCause += ")";
                        mDb.update(SoramameContract.FeedEntry.TABLE_NAME, values, strWhereCause, strWhereArg);
                    }
                }
                mDb.close();
            }catch (SQLiteException e){
                e.printStackTrace();
            }
        }

        return rc;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_select_station, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mList != null){ mList.clear(); }
    }

    @Override
    public void onPause()
    {
        // 都道府県インデックスを保存する
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("CurrentPref", mPref);
        editor.apply();

        // 測定局の選択状態をDBに保存
        setSelectedStation();

        super.onPause();
    }

    // 都道府県
    // 内部ストレージにファイル保存する
    // 都道府県名なので固定でも問題ないが。
    private class PrefSpinner extends AsyncTask<Void, Void, Void>
    {
        ArrayList<String> prefList = new ArrayList<String>();

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                if( getPrefInfo() > 0) {
                    FileOutputStream outfile = openFileOutput(SORAPREFFILE, Context.MODE_PRIVATE);

                    String url = String.format("%s%s", SORABASEURL, SORASUBURL);
                    Document doc = Jsoup.connect(url).get();
                    Elements elements = doc.getElementsByTag("option");

                    String strPref;
                    for (Element element : elements) {
                        if (Integer.parseInt(element.attr("value")) != 0) {
                            strPref = element.text();
                            prefList.add(strPref);
                            // ファイルから取得時に分割できるようにセパレータを追加する
                            outfile.write((strPref + ",").getBytes());
                        }
                    }
                    outfile.close();
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            // simple_spinner_itemはAndroidの初期設定
            ArrayAdapter<String> pref = new ArrayAdapter<String>(SelectStationActivity.this, R.layout.prefspinner_item, prefList);
            pref.setDropDownViewResource(R.layout.prefspinner_drop_item);
            // スピナーリスト設定
            Spinner prefSpinner = (Spinner)findViewById(R.id.spinner);
            if(prefSpinner != null) {
                prefSpinner.setAdapter(pref);
                prefSpinner.setSelection(mPref - 1);
            }
        }

        private int getPrefInfo()
        {
            int rc = 0 ;
            try
            {
                FileInputStream infile = openFileInput(SORAPREFFILE);
                int byteCount = infile.available();
                byte[] readBytes = new byte[byteCount];
                rc = infile.read(readBytes, 0, byteCount) ;
                String strBytes = new String(readBytes);
                infile.close();
                rc = 0;

                prefList.clear();
                String Pref[] = strBytes.split(",");
                Collections.addAll(prefList, Pref);
            }
            catch (FileNotFoundException e)
            {
                // ファイルが無ければそらまめサイトにアクセス
                rc = 1;
            }
            catch(IOException e)
            {
                rc = -1;
            }

            return rc;
        }
    }

    // 都道府県の測定局データ取得
    private class SoraStation extends AsyncTask<Boolean, Void, Void>
    {
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(SelectStationActivity.this);
            mProgressDialog.setTitle( "そらまめ（測定局取得）");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Boolean... params)
        {
            // テストで、データをDBに保存する。都道府県単位か全国か。
            // 既存DBに保存されているかをチェック。
            // 新規か更新か
            if( !params[0]) {
                mList = SoramameAccessor.getPref(SelectStationActivity.this, mPref);
            }
            else{
                mList = SoramameAccessor.updatePref(SelectStationActivity.this, mPref);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            // 測定局データ取得後にリスト表示
            ListView station = (ListView) findViewById(R.id.MonitorStation_selview);
            if(mList != null && station != null)
            {
                MonitoringStationAdapter Adapter = new MonitoringStationAdapter(SelectStationActivity.this, mList, true);
                station.setAdapter(Adapter);
            }
            mProgressDialog.dismiss();
        }
    }
}
