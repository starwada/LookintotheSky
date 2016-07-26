package com.lunarbase24.lookintothesky;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.RadioButton;

/*
    設定（セッティング）アクティビティ
    ウィジットの設定
    データ種別：PM2.5/OX
    表示時間：5～10時間
    更新時間：何分か（毎時）
    グラフ関連：背景の透過率？
 */
public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        try {
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(myToolbar);
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setIcon(R.drawable.ic_app_name);
            // Get a support ActionBar corresponding to this toolbar
            ActionBar ab = getSupportActionBar();
            // Enable the Up button
            ab.setDisplayHomeAsUpEnabled(true);

            RadioButton button = (RadioButton)findViewById(R.id.radioButton);
            button.setChecked(true);

        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }
}
