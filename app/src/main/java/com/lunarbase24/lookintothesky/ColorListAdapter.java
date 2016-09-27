package com.lunarbase24.lookintothesky;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by Wada on 2016/09/27.
 */

public class ColorListAdapter extends ArrayAdapter<CharSequence> {

    private int mSelItem;

    // コンストラクタ
    public ColorListAdapter(Context context, int resource, CharSequence[] objects, int select){
        super(context, resource, objects);
        mSelItem = select;
    }

    public static int getColor(String color){
        int retColor = 0;

        String tmp = color.toLowerCase();
        if(tmp.charAt(0) == '0' && tmp.charAt(1) == 'x'){
            tmp = tmp.substring(2);
        }

        // aarrggbb
        if(tmp.length() == 8){
            retColor = StrToHex(tmp.substring(0, 2)) << 24
                    | StrToHex(tmp.substring(2, 4)) << 16
                    | StrToHex(tmp.substring(4, 6)) << 8
                    | StrToHex(tmp.substring(6, 8));
         // rrggbb
        }else if(tmp.length() == 6){
            retColor = 0xff000000
                    | StrToHex(tmp.substring(0, 2)) << 16
                    | StrToHex(tmp.substring(2, 4)) << 8
                    | StrToHex(tmp.substring(4, 6));
        }
        return retColor;
    }

    public static int StrToHex(String str){
        int Hex = 0;

        try{
            Hex = Integer.parseInt(str,16);
        }catch (Exception e){
            e.printStackTrace();
            Hex = 0;
        }
        return Hex;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CharSequence colorId = this.getItem(position);

        // 行を作る
        LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
        View row = inflater.inflate(R.layout.settings_colorlistpreference_layout, parent, false);

        // 設定する色を取り出す
        int color = ColorListAdapter.getColor(colorId.toString());
        row.setId(color);

        // タイトルを設定する
        TextView tv = (TextView)row.findViewById(R.id.colorlist_text);
        tv.setText(colorId);
        tv.setTextColor(color);

        // チェックボックスのセット
        RadioButton rb = (RadioButton)row.findViewById(R.id.colorlist_check);
        // 選択されているものをチェックを入れる
        if (position == this.mSelItem) {
            rb.setChecked(true);
        }
        // クリックイベントに反応しないようにする
        rb.setClickable(false);

        // サンプル表示用ビューに色をセットする
        View view = row.findViewById(R.id.colorlist_color);
        view.setBackgroundColor(color);

        return row;
    }
}
