package com.lunarbase24.lookintothesky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

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

    // ウィジット関係
    // ウィジットIDとデータ種別登録
    public static int setWidgetID(Context context, int nWidgetID, int nType){
        int rc = 0;
        SoramameSQLHelper DbHelper = new SoramameSQLHelper(context);
        SQLiteDatabase Db = null;
        try {
            if (DbHelper == null) {
                return -1;
            }
            // まず、DBをチェックする。
            Db = DbHelper.getWritableDatabase();
            if (!Db.isOpen()) {
                return -2;
            }

            String[] selectionArgs = {String.valueOf(nWidgetID)};
            Cursor c = Db.query(SoramameContract.FeedEntry.WIDGET_TABLE, null,
                    SoramameContract.FeedEntry.COLUMN_NAME_WIDGETID + " = ?", selectionArgs, null, null, null);
            if (c.getCount() > 0) {
                // 登録済であれば、更新？
                if (c.moveToFirst()) {
                }
            }
            else{
                ContentValues values = new ContentValues();
                values.put(SoramameContract.FeedEntry.COLUMN_NAME_WIDGETID, nWidgetID);
                values.put(SoramameContract.FeedEntry.COLUMN_NAME_DATATYPE, nType);
                // 重複は追加しない
                long newRowId = Db.insertWithOnConflict(SoramameContract.FeedEntry.WIDGET_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            c.close();
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        finally {
            Db.close();
        }

        return rc;
    }

    // ウィジットIDとデータ種別問い合わせ
    public static int getWidgetID(Context context, int nWidgetID){
        int type = 0;
        SoramameSQLHelper DbHelper = new SoramameSQLHelper(context);
        SQLiteDatabase Db = null;
        try {
            if (DbHelper == null) {
                return -1;
            }
            // まず、DBをチェックする。
            Db = DbHelper.getWritableDatabase();
            if (!Db.isOpen()) {
                return -2;
            }

            String[] selectionArgs = {String.valueOf(nWidgetID)};
            Cursor c = Db.query(SoramameContract.FeedEntry.WIDGET_TABLE, null,
                    SoramameContract.FeedEntry.COLUMN_NAME_WIDGETID + " = ?", selectionArgs, null, null, null);
            if (c.getCount() > 0) {
                // 登録済であれば、更新？
                if (c.moveToFirst()) {
                    type = c.getInt(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_DATATYPE));
                }
            }
            c.close();
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        finally {
            Db.close();
        }

        return type;
    }

    // ウィジットIDとデータ種別削除
    public static int deleteWidgetID(Context context, int nWidgetID){
        int rc = 0;
        SoramameSQLHelper DbHelper = new SoramameSQLHelper(context);
        SQLiteDatabase Db = null;
        try {
            if (DbHelper == null) {
                return -1;
            }
            // まず、DBをチェックする。
            Db = DbHelper.getWritableDatabase();
            if (!Db.isOpen()) {
                return -2;
            }

            String[] selectionArgs = {String.valueOf(nWidgetID)};
            rc = Db.delete(SoramameContract.FeedEntry.WIDGET_TABLE,
                    SoramameContract.FeedEntry.COLUMN_NAME_WIDGETID + " = ?", selectionArgs);
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        finally {
            Db.close();
        }

        return rc;
    }

    // 測定局関係
    // 指定測定局コードのデータ取得
    public static int getStation(Context context, int nCode, String station[]){
        int rc = 0;
        SoramameSQLHelper DbHelper = new SoramameSQLHelper(context);
        SQLiteDatabase Db = null;

        try {
            if (DbHelper == null) {
                return -1;
            }
            // まず、DBをチェックする。
            Db = DbHelper.getReadableDatabase();
            if (!Db.isOpen()) {
                return -2;
            }

            String strWhereArg[] = {String.valueOf(nCode)};
            // 日付でソート desc 降順（新しい->古い）
            Cursor c = Db.query(SoramameContract.FeedEntry.TABLE_NAME, null,
                    SoramameContract.FeedEntry.COLUMN_NAME_CODE + " = ?", strWhereArg, null, null, null);
            rc = c.getCount();
            if (rc > 0){
                if (c.moveToFirst()) {
                    station[0] = c.getString(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_STATION));
                    station[1] = c.getString(c.getColumnIndexOrThrow(SoramameContract.FeedEntry.COLUMN_NAME_ADDRESS));
                }
            }
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return rc;
    }

    // 指定都道府県の測定局データを取得
    // まずDBをクエリーし、なければWebから取得しDBに登録する。
    // int nPref 都道府県コード
    // ArrayList<Soramame> 測定局データリスト
    public static int getPref(Context context, int nPrefCode, ArrayList<Soramame> list) {
        int rc = 0;
        SoramameSQLHelper DbHelper = new SoramameSQLHelper(context);
        SQLiteDatabase Db = null;
        try {
            if (DbHelper == null) {
                return -1;
            }
            // まず、DBをチェックする。
            Db = DbHelper.getReadableDatabase();
            if (!Db.isOpen()) {
                return -2;
            }

            String[] selectionArgs = {String.valueOf(nPrefCode)};
            Cursor c = Db.query(SoramameContract.FeedEntry.TABLE_NAME, null,
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
                Db.close();
                return rc;
            }
            c.close();
            Db.close();

            // DBに無ければ、検索してDBに登録する。
            String strOX;
            String strPM25;
            String strWD;

            Db = DbHelper.getWritableDatabase();

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
                                long newRowId = Db.insertWithOnConflict(SoramameContract.FeedEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                            }
                            //}
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (SQLiteException e){
            e.printStackTrace();
        }
        finally {
            Db.close();
        }

        return rc;
    }
}