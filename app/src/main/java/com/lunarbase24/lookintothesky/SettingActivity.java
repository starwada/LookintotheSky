package com.lunarbase24.lookintothesky;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.util.ArrayList;

/*
    設定（セッティング）アクティビティ
    ウィジットの設定
    データ種別：PM2.5/OX
    表示時間：6～12時間
    更新時間：何分か（毎時）
    グラフ関連：背景の透過率？グラフ丸の半径
 */
public class SettingActivity extends AppCompatActivity {
    private AppSettings m_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        m_settings = (AppSettings)this.getApplication();

        try {
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(myToolbar);
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setIcon(R.drawable.ic_app_name);
            // Get a support ActionBar corresponding to this toolbar
            ActionBar ab = getSupportActionBar();
            // Enable the Up button
            ab.setDisplayHomeAsUpEnabled(true);


            // 表示時間スピナー
            ArrayList<String> dataList = new ArrayList<String>();
            for(int i=0; i<7; i++){
                dataList.add(String.format("%d 時間", i+6));
            }
            ArrayAdapter<String> hour = new ArrayAdapter<String>(SettingActivity.this, android.R.layout.simple_spinner_item, dataList);
            hour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // スピナーリスト設定
            Spinner DispHour = (Spinner) findViewById(R.id.DispHourSpinner);
            if (DispHour != null) {
                DispHour.setAdapter(hour);
                DispHour.setSelection(m_settings.DispHourIndex());
                DispHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        m_settings.m_nDispHour = i+6;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            // 更新時間スピナー
            ArrayList<String> updateList = new ArrayList<String>();
            for(int i=0; i<12; i++){
                updateList.add(String.format("%d 分", i*5));
            }
            ArrayAdapter<String> updatetime = new ArrayAdapter<String>(SettingActivity.this, android.R.layout.simple_spinner_item, updateList);
            updatetime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // スピナーリスト設定
            Spinner UpdateTimeSpinner = (Spinner) findViewById(R.id.UpdateTimeSpinner);
            if (UpdateTimeSpinner != null) {
                UpdateTimeSpinner.setAdapter(updatetime);
                UpdateTimeSpinner.setSelection(m_settings.UpdateTimeIndex());
                UpdateTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        m_settings.m_nUpdateTime = i*5;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            SeekBar trans = (SeekBar)findViewById(R.id.seekBar);
            trans.setMax(255);
            trans.setProgress(128);

        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        // 画面の設定を取得
        // 表示データ種別

        super.onPause();
    }
}
