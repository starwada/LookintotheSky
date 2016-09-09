package com.lunarbase24.lookintothesky;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Environment;
import android.text.TextPaint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Wada on 2016/06/27.
 * ウィジットの画像用
 */
public class GraphFactory {

    // 以下を引数とする。
    // グラフ背景の透過率、表示時間、表示データ種別
    static public Bitmap drawGraph(Soramame soramame, int appWidgetId, int nType, AppSettings settings){
        int rc =0;
        TextPaint mTextPaint;
        Paint mBack;
        Paint mLine ;
        Paint mDot ;
        RectF mRect;

        Paint mOX;
        // 表示区分 PM2.5 OX（光化学オキシダント） WS（風速）
        float mDotY[][] = { {10.0f, 15.0f, 35.0f, 50.0f, 70.0f, 100.0f },
                {0.02f, 0.04f, 0.06f, 0.12f, 0.24f, 0.34f },
                {4.0f, 7.0f, 10.0f, 13.0f, 15.0f, 25.0f}};

        int mMode;                      // 表示データモード 0 PM2.5/1 OX/2 風速
        int mDispHour = settings.m_nDispHour;              // 表示時間

        mBack = new Paint();
        mBack.setColor(Color.argb(75, 0, 0, 255));
        mLine = new Paint();
        mLine.setColor(Color.argb(125, 0, 0, 0));
        mLine.setStrokeWidth(4);
        mDot = new Paint();
        mDot.setColor(Color.argb(255, 255, 0, 0));
        mDot.setStrokeWidth(2);
        mRect = new RectF();
        mMode = nType;
        // OX用のペイント情報
        mOX = new Paint();
        mOX.setColor(Color.argb(75, 255, 0, 0));
        mOX.setStrokeWidth(2.4f);
        float TextHeight = 3.0f;

        int nWidth = 500;
        int nHeight = 300;
        Bitmap graph = Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(graph);
        canvas.drawColor(Color.TRANSPARENT);

        int paddingLeft = 30;
        int paddingTop = 40;
        int paddingRight = 30;
        int paddingBottom = 40;

        int contentWidth = nWidth - paddingLeft - paddingRight;
        int contentHeight = nHeight - paddingTop - paddingBottom;

        // グラフ描画
        // グラフ背景
        float y = (float)(paddingTop+contentHeight);
        float rh = (float)contentHeight/mDotY[mMode][5];

        int nTransparent = settings.m_nTransp;
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        // PM2.5/OX/WS
        // ～１０/0.0-0.02/0.2-3.9
        mRect.set( (float)paddingLeft, y-rh*mDotY[mMode][0], (float)(paddingLeft+contentWidth), y);
        mBack.setColor(Color.argb(nTransparent, 0, 0, 255));
//        mBack.setColor(0xFF2196F3);
        canvas.drawRect(mRect, mBack);
        // １１～１５/0.021-0.04/4.0-6.9
        mRect.set( (float)paddingLeft, y-rh*mDotY[mMode][1], (float)(paddingLeft+contentWidth), y-rh*mDotY[mMode][0]);
        mBack.setColor(Color.argb(nTransparent, 0, 255,255));
//        mBack.setColor(0xFF81D4FA);
        canvas.drawRect(mRect, mBack);
        // １６～３５/0.041-0.06/7.0-9.9
        mRect.set( (float)paddingLeft, y-rh*mDotY[mMode][2], (float)(paddingLeft+contentWidth), y-rh*mDotY[mMode][1]);
        mBack.setColor(Color.argb(nTransparent, 0, 255,128));
        canvas.drawRect(mRect, mBack);
        // ３６～５０/0.061-0.119/10.0-12.9
        mRect.set( (float)paddingLeft, y-rh*mDotY[mMode][3], (float)(paddingLeft+contentWidth), y-rh*mDotY[mMode][2]);
        mBack.setColor(Color.argb(nTransparent, 255, 255,0));
        canvas.drawRect(mRect, mBack);
        // ５１～７０/0.12-0.239/13.0-14.9
        mRect.set( (float)paddingLeft, y-rh*mDotY[mMode][4], (float)(paddingLeft+contentWidth), y-rh*mDotY[mMode][3]);
        mBack.setColor(Color.argb(nTransparent, 255, 128,0));
        canvas.drawRect(mRect, mBack);
        // 70-100/0.24-0.34/15.0-25.0
        mRect.set( (float)paddingLeft, y-rh*mDotY[mMode][5], (float)(paddingLeft+contentWidth), y-rh*mDotY[mMode][4]);
        mBack.setColor(Color.argb(nTransparent, 255, 0,0));
        canvas.drawRect(mRect, mBack);

        // グラフ枠
        mLine.setStrokeWidth(4);
        canvas.drawLine(paddingLeft, paddingTop + contentHeight, paddingLeft + contentWidth, paddingTop + contentHeight, mLine);
        canvas.drawLine( paddingLeft, paddingTop, paddingLeft, contentHeight+paddingTop, mLine );
        y = (float)(paddingTop+contentHeight);
        mLine.setStrokeWidth(1);
        mTextPaint.setTextSize(30.0f);
        mTextPaint.setColor(Color.WHITE);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        // ほぼ文字高さのようなので、マイナスで返るので反転
        TextHeight = -fontMetrics.ascent;

        for(int i=0; i<4; i++){
            y -= (float)contentHeight/5;
            canvas.drawLine(paddingLeft, y, paddingLeft + contentWidth, y, mLine);
            switch(mMode){
                case 0:
                    canvas.drawText(String.format("%d", i*20+20), 0, y + TextHeight/2, mTextPaint);
                    break;
                case 1:
                    canvas.drawText(String.format("%.2f", i*mDotY[mMode][5]/5.0f+mDotY[mMode][5]/5.0f), 0, y + TextHeight/2, mTextPaint);
                    break;
                case 2:
                    canvas.drawText(String.format("%.1f", i*mDotY[mMode][5]/5.0f+mDotY[mMode][5]/5.0f), 0, y + TextHeight/2, mTextPaint);
                    break;
            }
        }

        String strHour;
        // グラフ
        if(soramame.getSize() > 0){
            ArrayList<Soramame.SoramameData> list = soramame.getData();
            float x=paddingLeft+contentWidth;
            // ここで、時間（データ数）での分割
            // listには新しいデータから入っている
            float gap = (float)contentWidth/(mDispHour+1) ;
            x -= gap;

            y = (float)(paddingTop + contentHeight);

            int nCount=0;
            float doty = 0f;
            float fradius = settings.m_fRadius;
            float fOXY[] = { 0.0f, 0.0f  };
            for( Soramame.SoramameData data : list){
                if( nCount >= mDispHour){ break; }
                //if( mDispDay != 0 && nCount > mDispDay*24 ){ break; }
                switch(mMode){
                    case 0:
                        doty = y-(data.getPM25() * (float)contentHeight/mDotY[mMode][5]);
                        break;
                    case 1:
                        doty = y-(data.getOX() * (float)contentHeight/mDotY[mMode][5]);
                        break;
                    case 2:
                        doty = y-(data.getWS() * (float)contentHeight/mDotY[mMode][5]);
                        break;
                }

                if( (mMode == 0 && data.getPM25() > 0) ||
                        (mMode == 1 && data.getOX() > 0.0) ||
                        (mMode == 2 && data.getWS() > 0.0 )) {
                    canvas.drawCircle(x, doty, fradius, mDot);
                }
                // 時間軸描画
                if( data.getDate().get(Calendar.HOUR_OF_DAY) == 0 ){
                    canvas.drawLine(x, paddingTop, x, contentHeight + paddingTop, mLine);
                }
//                if( data.getDate().get(Calendar.HOUR_OF_DAY) == 1 ){
                    // 時間描画
                strHour = String.format(Locale.ENGLISH, "%d", data.getDate().get(Calendar.HOUR_OF_DAY));
                    canvas.drawText(strHour, x-mTextPaint.measureText(strHour)/2, paddingTop+contentHeight+TextHeight, mTextPaint);
//                }
                // リストにてクリックしたインデックスデータに描画<-ここを画像に切替える
                nCount += 1;
                x -= gap;
                // グラフ用ポリライン描画
                fOXY[1] = fOXY[0];
                fOXY[0] = (doty > y ? y : doty);
                if( nCount > 1){
                    canvas.drawLine(x+gap, fOXY[0], x+gap+gap, fOXY[1], mOX);
                }
            }
        }
        // 測定局
        mTextPaint.setTextSize(40.0f);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setColor(Color.WHITE);
        fontMetrics = mTextPaint.getFontMetrics();
        // ほぼ文字高さのようなので、マイナスで返るので反転
        TextHeight = -fontMetrics.ascent;
        canvas.drawText(String.format("%s", soramame.getMstName()), paddingLeft*0.5f, paddingTop + TextHeight*0.5f, mTextPaint);
        // データ種別と最新日時
        canvas.drawText(String.format(Locale.JAPANESE, "%s %s", (mMode == 0 ? "PM2.5" : "   OX"), soramame.getData().get(0).getDateString()), paddingLeft*1.2f, paddingTop + TextHeight*1.5f, mTextPaint);

        // 読み書きするファイル名を指定
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + String.format("/soracapture_%d_%d.png", appWidgetId, mMode));
        // 指定したファイル名が無ければ作成する。
        // SDKバージョンの影響等（パーミッション）以下がエラーとなるので、一旦コメントとする。
//        if( file.getParentFile().mkdir() ) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file, false);
                // 画像のフォーマットと画質と出力先を指定して保存
                // 100で165KB、値を半分にすると1/4に減る
                graph.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ie) {
                        fos = null;
                    }
                }
            }
//        }

        return graph;
    }
}
