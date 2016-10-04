package com.lunarbase24.lookintothesky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.ListPreference;
import android.preference.Preference;
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
 */

public class ColorListPreference extends ListPreference {
    private int[] mGraphBackColor;      // グラフ背景色
    private int mIndex;

    public int getColor(String color){

        return mGraphBackColor[Integer.parseInt(color)];
//        int retColor = 0;
//
//        String tmp = color.toLowerCase();
//        if(tmp.charAt(0) == '0' && tmp.charAt(1) == 'x'){
//            tmp = tmp.substring(2);
//        }
//
//        // aarrggbb
//        if(tmp.length() == 8){
//            retColor = StrToHex(tmp.substring(0, 2)) << 24
//                    | StrToHex(tmp.substring(2, 4)) << 16
//                    | StrToHex(tmp.substring(4, 6)) << 8
//                    | StrToHex(tmp.substring(6, 8));
//            // rrggbb
//        }else if(tmp.length() == 6){
//            retColor = 0xff000000
//                    | StrToHex(tmp.substring(0, 2)) << 16
//                    | StrToHex(tmp.substring(2, 4)) << 8
//                    | StrToHex(tmp.substring(4, 6));
//        }
//        return retColor;
    }

    public int StrToHex(String str){
        int Hex = 0;

        try{
            Hex = Integer.parseInt(str,16);
        }catch (Exception e){
            e.printStackTrace();
            Hex = 0;
        }
        return Hex;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            CharSequence colorId = this.getItem(position);

            // 行を作る
            LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
            View row = inflater.inflate(R.layout.settings_colorlistpreference_layout, parent, false);

            // クリックリスナー
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    SharedPreferences.Editor editor = getSharedPreferences().edit();
//                    editor.putInt( getKey(), position );
//                    editor.apply();
                    mIndex = position;

                    getDialog().dismiss();
                }
            });

            // 設定する色を取り出す
            int color = getColor(colorId.toString());
//            row.setId(color);

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
            view.setColor(color);

            return row;
        }
    }

    public ColorListPreference(Context context, AttributeSet attrs){
        super(context, attrs);
        mGraphBackColor = context.getResources().getIntArray(R.array.graph_color_rgb);
        mIndex = 0;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder){

        // Preferenceにはインデックスを保持しておく
        int nColorIndex = this.getSharedPreferences().getInt(this.getKey(), 0);
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
        if(!positiveResult) {
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            String key = this.getKey();
            editor.putInt(key, mIndex);
            editor.apply();
        }
        super.onDialogClosed(positiveResult);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        // プリファレンスから色を取り出す
        int nColorIndex = this.getSharedPreferences().getInt(this.getKey(), 0);
        CharSequence[] values =  this.getEntryValues();
        if(nColorIndex < 0 || values.length <= nColorIndex){
            nColorIndex = 0;
        }

        // カラーサンプルの色を設定する
        ColorView v = (ColorView)view.findViewById(R.id.color_sample);
        v.setColor(getColor(values[nColorIndex].toString()));
    }
}
