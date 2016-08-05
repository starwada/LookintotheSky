package com.lunarbase24.lookintothesky;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    private AppSettings update;
    private static final String ACTION_CHANGE_SETTING = "com.lunarbase24.lookintothesky.ACTION_CHANGE_SETTING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        m_settings = (AppSettings)this.getApplication();
        update = new AppSettings();

        try {
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(myToolbar);
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setIcon(R.drawable.ic_app_name);
            // Get a support ActionBar corresponding to this toolbar
            ActionBar ab = getSupportActionBar();
            // Enable the Up button
            ab.setDisplayHomeAsUpEnabled(true);

            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            m_settings.m_nDispHour = sharedPref.getInt("DispHour", update.m_nDispHour);
            m_settings.m_nUpdateTime = sharedPref.getInt("UpdateTime", update.m_nUpdateTime);

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
                        update.m_nDispHour = i+6;
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
                        update.m_nUpdateTime = i*5;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }

            SeekBar trans = (SeekBar)findViewById(R.id.seekBar);
            trans.setMax(255);
            trans.setProgress(m_settings.m_nTransp);
            TextView view = (TextView)findViewById(R.id.textView6);
            view.setText(String.format("%s:%d", getString(R.string.graph_back), m_settings.m_nTransp));
            trans.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    update.m_nTransp = i;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    TextView view = (TextView)findViewById(R.id.textView6);
                    view.setText(String.format("%s:%d", getString(R.string.graph_back), update.m_nTransp));
                    Toast toast = Toast.makeText(SettingActivity.this, String.format("%d", update.m_nTransp), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.START, (int)seekBar.getX(), (int)seekBar.getY());
                    toast.show();
                }
            });

            SeekBar radius = (SeekBar)findViewById(R.id.seekBar2);
            radius.setMax(20);
            radius.setProgress((int)m_settings.m_fRadius);
            view = (TextView)findViewById(R.id.textView7);
            view.setText(String.format("%s:%.1f", getString(R.string.graph_radius), m_settings.m_fRadius));
            radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    update.m_fRadius = (float)i;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    TextView view = (TextView)findViewById(R.id.textView7);
                    view.setText(String.format("%s:%.1f", getString(R.string.graph_radius), update.m_fRadius));
                    Toast toast = Toast.makeText(SettingActivity.this, String.format("%.1f", update.m_fRadius), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.START, (int)seekBar.getX(), (int)seekBar.getY());
                    toast.show();
                }
            });

            Button setting = (Button)findViewById(R.id.button);
            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 同じなら未処理
                    if(m_settings.isEqual(update)){
                        return;
                    }
                    m_settings.set(update);
                    // 更新通知
                    Intent alarmIntent = new Intent(SettingActivity.this, SoraAppWidget.class);
                    alarmIntent.setAction(ACTION_CHANGE_SETTING);
                    PendingIntent operation = PendingIntent.getBroadcast(SettingActivity.this, 0, alarmIntent, 0);
                    AlarmManager am = (AlarmManager)SettingActivity.this.getSystemService(Context.ALARM_SERVICE);
                    am.set(AlarmManager.RTC, 0, operation);
                }
            });
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        update.set(m_settings);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("DispHour", update.m_nDispHour);
        editor.putInt("UpdateTime", update.m_nUpdateTime);
        editor.apply();
        super.onPause();
    }
}
