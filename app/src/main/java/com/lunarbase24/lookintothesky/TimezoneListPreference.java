package com.lunarbase24.lookintothesky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.ListPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Created by Wada on 2016/10/05.
 * 通知時間帯の設定用
 * ListPreferenceをベースに、チェックボックスにて複数選択に対応
 * SharedPreferenceに保持するのは、項目のフラグビット。
 * EntryValuesはインデックスとしている。
 */

public class TimezoneListPreference extends ListPreference {
    private int mCheck;     // 項目のフラグビット

    public class TimezoneListAdapter extends ArrayAdapter<CharSequence> {

        private CharSequence[] mEntries;        // 時間帯の文字列

        // コンストラクタ
        public TimezoneListAdapter(Context context, int resource, CharSequence[] objects, int check, CharSequence[] entries){
            super(context, resource, objects);
            mCheck = check;
            mEntries = entries;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final CharSequence index = this.getItem(position);

            // 行を作る
            LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
            View row = inflater.inflate(R.layout.settings_timezonelistpreference_layout, parent, false);

            // タイトルを設定する
            TextView tv = (TextView)row.findViewById(R.id.timezonelist_text);
            tv.setText(mEntries[position]);
            tv.setTextColor(Color.BLACK);

            // チェックボックスのセット
            CheckBox cb = (CheckBox) row.findViewById(R.id.timezonelist_check);
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // positionとフラグをみて、値を設定する。
                    int pos = v.getId();
                    int bit = 0;
                    if( ((CheckBox)v).isChecked()){
                        bit = 1 << pos;
                        mCheck |= bit;
                    }
                    else{
                        bit = ~(1 << pos);
                        mCheck &= bit;
                    }
                }
            });

            // 選択されているものをチェックを入れる
            int mask = 1 << position;
            if((mCheck & mask) != 0){
                cb.setChecked(true);
            }
            else{
                cb.setChecked(false);
            }
            cb.setId(position);

            return row;
        }
    }

    public TimezoneListPreference(Context context, AttributeSet attrs){
        super(context, attrs);
        mCheck = 0;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder){
        // 親を先に実行しておく、そうしないと以下のsetPositiveButton()が効かない。
        super.onPrepareDialogBuilder(builder);

        // Preferenceにはインデックスを保持しておく
        String sCheck = this.getSharedPreferences().getString(this.getKey(), "0");
        int nCheck = Integer.parseInt(sCheck);
        // リストアダプターを作る
        ListAdapter la = (ListAdapter)new TimezoneListPreference.TimezoneListAdapter(this.getContext(),
                R.layout.settings_timezonelistpreference_layout, this.getEntryValues(), nCheck, this.getEntries());

        // リストアダプターをセットする
        builder.setAdapter(la, null);
        // OKボタンを有効にする、クリックリスナーも設定
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSharedPreferences().edit();
                editor.putString( getKey(), String.format("%d", mCheck) );
                editor.apply();
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
    }

    @Override
    public String getValue() {
        // プリファレンスから色を取り出す
        String sCheck = this.getSharedPreferences().getString(this.getKey(), "0");
        int nCheck = Integer.parseInt(sCheck);
        CharSequence[] values =  this.getEntries();

        String sValue = "";
        int mask = 1;
        for(int i=0; i<4; i++){
            mask = 1 << i;
            if( (nCheck & mask) != 0 ){
                if(sValue == ""){
                    sValue = values[i].toString();
                }
                else {
                    sValue = String.format("%s\n%s", sValue, values[i].toString());
                }
            }
        }

        return sValue;
    }

    @Override
    public CharSequence getEntry() {
        // プリファレンスから色を取り出す
        String sCheck = this.getSharedPreferences().getString(this.getKey(), "0");
        return sCheck.subSequence(0, 1);
    }
}
