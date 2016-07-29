package com.lunarbase24.lookintothesky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Wada on 2016/07/29.
 * そらまめデータアクセッサー
 * このクラスでしてもらいたいこと
 * １：指定都道府県の測定局データを取得
 * ２：測定局の選択状態管理（設定、解除）
 * ３：指定測定局の計測データ取得
 * DBおよびWebへの問い合わせ等を行う
 * DB：測定局管理テーブル、測定データ管理テーブル
 */
public class SoramameAccessor {
    private static final String SORAPREFFILE = "SoraPrefFile";

    private static  final  String SORABASEURL="http://soramame.taiki.go.jp/";
    private static final String SORASUBURL ="MstItiran.php";
    private static final String SORADATAURL = "DataList.php?MstCode=";
    // 指定都道府県の測定局一覧取得
    private static final String SORAPREFURL ="MstItiranFrame.php?Pref=";

    private SoramameSQLHelper mDbHelper = null;
    private SQLiteDatabase mDb = null;

    public void SoramameAccessor(Context context){
        mDbHelper = new SoramameSQLHelper(context);
    }

    // 指定都道府県の測定局データを取得
    // まずDBをクエリーし、なければWebから取得しDBに登録する。
    // int nPref 都道府県コード
    // ArrayList<Soramame> 測定局データリスト
    public int getPref(int nPrefCode, ArrayList<Soramame> list) {
        int rc = 0;
        try {
            if (mDbHelper == null) {
                return -1;
            }
            // まず、DBをチェックする。
            mDb = mDbHelper.getReadableDatabase();
            if (!mDb.isOpen()) {
                return -2;
            }

            String[] selectionArgs = {String.valueOf(nPrefCode)};
            Cursor c = mDb.query(SoramameContract.FeedEntry.TABLE_NAME, null,
                    SoramameContract.FeedEntry.COLUMN_NAME_PREFCODE + " = ?", selectionArgs, null, null, null);
            if (c.getCount() > 0) {
                // DBにデータがあれば、DBから取得する。
                if (c.moveToFirst()) {
                    if (list != null) {
                        list.clear();
                    }
                    list = new ArrayList<Soramame>();
                    while (true) {
                        Soramame mame = new Soramame(
                                c.getInt(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_CODE)),
                                c.getString(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_STATION)),
                                c.getString(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_ADDRESS)));
                        mame.setAllow(
                                c.getInt(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_OX)),
                                c.getInt(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_PM25)),
                                c.getInt(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_WD))
                        );
                        mame.setSelected(c.getInt(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_SEL)));
                        list.add(mame);

                        if (!c.moveToNext()) {
                            break;
                        }
                    }
                }
                c.close();
                mDb.close();
                return rc;
            }
            c.close();
            mDb.close();

            // DBに無ければ、検索してDBに登録する。
            String strOX;
            String strPM25;
            String strWD;

            mDb = mDbHelper.getWritableDatabase();

            String url = String.format(Locale.ENGLISH, "%s%s%d", SORABASEURL, SORAPREFURL, nPrefCode);
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.getElementsByAttributeValue("name", "Hyou");
            for (Element element : elements) {
                if (element.hasAttr("src")) {
                    url = element.attr("src");
                    String soraurl = SORABASEURL + url;

                    Document sora = Jsoup.connect(soraurl).get();
                    Element body = sora.body();
                    Elements tables = body.getElementsByTag("tr");
                    url = "";
                    Integer cnt = 0;
                    if (list != null) {
                        list.clear();
                    }
                    list = new ArrayList<Soramame>();

                    for (Element ta : tables) {
                        if (cnt++ > 0) {
                            Elements data = ta.getElementsByTag("td");
                            // 測定対象取得 OX(8)、PM2.5(13)、風向(15)
                            // 想定は○か✕
                            strOX = data.get(8).text();
                            strPM25 = data.get(13).text();
                            strWD = data.get(15).text();
                            // 最後のデータが空なので
                            if (strPM25.length() < 1) {
                                break;
                            }

                            int nCode = strPM25.codePointAt(0);
                            // PM2.5測定局のみ ○のコード(9675)
                            //if( nCode == 9675 ) {
                            Soramame ent = new Soramame(Integer.parseInt(data.get(0).text()), data.get(1).text(), data.get(2).text());
                            if (ent.setAllow(strOX, strPM25, strWD)) {
                                list.add(ent);

                                // 測定局DBに保存
                                ContentValues values = new ContentValues();
                                values.put(SoramameContract.FeedEntry.COLUMN_NAME_IND, cnt);
                                values.put(SoramameContract.FeedEntry.COLUMN_NAME_STATION, data.get(1).text());
                                values.put(SoramameContract.FeedEntry.COLUMN_NAME_CODE, Integer.valueOf(data.get(0).text()));
                                values.put(SoramameContract.FeedEntry.COLUMN_NAME_ADDRESS, data.get(2).text());
                                values.put(SoramameContract.FeedEntry.COLUMN_NAME_PREFCODE, nPrefCode);
                                values.put(SoramameContract.FeedEntry.COLUMN_NAME_OX, ent.getAllow(0) ? 1 : 0);
                                values.put(SoramameContract.FeedEntry.COLUMN_NAME_PM25, ent.getAllow(1) ? 1 : 0);
                                values.put(SoramameContract.FeedEntry.COLUMN_NAME_WD, ent.getAllow(2) ? 1 : 0);
                                values.put(SoramameContract.FeedEntry.COLUMN_NAME_SEL, 0);
                                // 重複は追加しない
                                long newRowId = mDb.insertWithOnConflict(SoramameContract.FeedEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                            }
                            //}
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rc;
    }
}
