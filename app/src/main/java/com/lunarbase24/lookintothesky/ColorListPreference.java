package com.lunarbase24.lookintothesky;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;

/**
 * Created by Wada on 2016/09/27.
 */

public class ColorListPreference extends ListPreference implements DialogInterface.OnClickListener{

    public ColorListPreference(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder){
        super.onPrepareDialogBuilder(builder);

        // Preferenceにはインデックスを保持しておく
        int nColorIndex = this.getSharedPreferences().getInt(this.getKey(), 0);
        CharSequence[] values =  this.getEntryValues();
        if(nColorIndex < 0 || values.length <= nColorIndex){
            nColorIndex = 0;
        }
        // リストアダプターを作る
        ListAdapter la = (ListAdapter)new ColorListAdapter(this.getContext(),
                R.layout.settings_colorlistpreference_layout, this.getEntryValues(), nColorIndex);

        // リストアダプターをセットする
        builder.setAdapter(la, this);

        return;
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
        View v = view.findViewById(R.id.color_sample);
        v.setBackgroundColor(Integer.parseInt(values[nColorIndex].toString()));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        CharSequence[] values =  this.getEntryValues();
        if(which < 0 || values.length <= which){
            which = 0;
        }

        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt( getKey(), which );
        editor.commit();

        dialog.dismiss();
    }
}
