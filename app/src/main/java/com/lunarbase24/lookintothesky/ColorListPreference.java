package com.lunarbase24.lookintothesky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.ListPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by Wada on 2016/09/27.
 * EntryValuesには色配列のインデックスを文字列にて設定している。
 * EntryValuesは文字列となっている。
 * Preferenceにはそのインデックスを文字列にて保持。
 * ここを、intで設定して、エラーが起き、原因を特定するのに時間がかかった。
 * 色配列は別のarrayにて定義している（他でも利用できるように）。
 *
 */

public class ColorListPreference extends ListPreference {
    private int[] mGraphBackColor;      // グラフ背景色

    public int getColor(String color){
        return mGraphBackColor[Integer.parseInt(color)];
    }

    public class ColorListAdapter extends ArrayAdapter<CharSequence> {

        private int mSelItem;
        private CharSequence[] mEntries;

        // コンストラクタ
        public ColorListAdapter(Context context, int resource, CharSequence[] objects, int select, CharSequence[] entries){
            super(context, resource, objects);
            mSelItem = select;
            mEntries = entries;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final CharSequence colorId = this.getItem(position);

            // 行を作る
            LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
            View row = inflater.inflate(R.layout.settings_colorlistpreference_layout, parent, false);

            // クリックリスナー
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = getSharedPreferences().edit();
                    editor.putString( getKey(), colorId.toString() );
                    editor.apply();

                    getDialog().dismiss();
                }
            });

            // タイトルを設定する
            TextView tv = (TextView)row.findViewById(R.id.colorlist_text);
            tv.setText(mEntries[position]);
            tv.setTextColor(Color.BLACK);

            // チェックボックスのセット
            RadioButton rb = (RadioButton)row.findViewById(R.id.colorlist_check);
            // 選択されているものをチェックを入れる
            if (position == this.mSelItem) {
                rb.setChecked(true);
            }
            // クリックイベントに反応しないようにする
            rb.setClickable(false);

            // サンプル表示用ビューに色をセットする
            ColorView view = (ColorView)row.findViewById(R.id.colorlist_color);
            view.setColor(getColor(colorId.toString()));

            return row;
        }
    }

    public ColorListPreference(Context context, AttributeSet attrs){
        super(context, attrs);
        mGraphBackColor = context.getResources().getIntArray(R.array.graph_color_rgb);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder){

        // Preferenceにはインデックスを保持しておく
        String sColorIndex = this.getSharedPreferences().getString(this.getKey(), "0");
        int nColorIndex = Integer.parseInt(sColorIndex);
        CharSequence[] values =  this.getEntryValues();
        if(nColorIndex < 0 || values.length <= nColorIndex){
            nColorIndex = 0;
        }
        // リストアダプターを作る
        ListAdapter la = (ListAdapter)new ColorListAdapter(this.getContext(),
                R.layout.settings_colorlistpreference_layout, this.getEntryValues(), nColorIndex, this.getEntries());

        // リストアダプターをセットする
        builder.setAdapter(la, null);
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        // カラーサンプルの色を設定する
        ColorView v = (ColorView)view.findViewById(R.id.color_sample);
        v.setColor(getColor(getEntry().toString()));
    }

    @Override
    public String getValue() {
        // プリファレンスから色を取り出す
        String sColorIndex = this.getSharedPreferences().getString(this.getKey(), "0");
        int nColorIndex = Integer.parseInt(sColorIndex);
        CharSequence[] values =  this.getEntries();
        if(nColorIndex < 0 || values.length <= nColorIndex){
            nColorIndex = 0;
        }
        return values[nColorIndex].toString();
    }

    @Override
    public CharSequence getEntry() {
        // プリファレンスから色を取り出す
        String sColorIndex = this.getSharedPreferences().getString(this.getKey(), "0");
        int nColorIndex = Integer.parseInt(sColorIndex);
        CharSequence[] values =  this.getEntryValues();
        if(nColorIndex < 0 || values.length <= nColorIndex){
            nColorIndex = 0;
        }
        return values[nColorIndex];
    }
}
