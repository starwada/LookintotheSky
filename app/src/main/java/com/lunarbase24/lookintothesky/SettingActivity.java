package com.lunarbase24.lookintothesky;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.SeekBar;

/*
    設定（セッティング）アクティビティ
    ウィジットの設定
    データ種別：PM2.5/OX
    表示時間：6～12時間
    更新時間：何分か（毎時）
    グラフ関連：背景の透過率？グラフ丸の半径
 */
public class SettingActivity extends AppCompatActivity {
    class GraphSettings{
        int m_nDataType;        // データ種別 0 PM2.5/1 OX
        int m_nDispHour;        // 表示時間 6～12時間
        int m_nUpdateTime;  // 更新時間 分
        int m_nTransp;        // グラフ背景透過率 0～255
        float m_fRadius;        // グラフ丸の半径

        void GraphSettings(){
            m_nDataType = 0;
            m_nDispHour = 12;
            m_nUpdateTime = 15;
            m_nTransp = 128;
            m_fRadius = 8.0f;
        }
    }

    private GraphSettings m_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        m_settings = new GraphSettings();

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

            // 表示時間ピッカー
            NumberPicker DispHour = (NumberPicker)findViewById(R.id.numberPicker);
            DispHour.setMinValue(6);
            DispHour.setMaxValue(12);
            DispHour.setValue(12);
            DispHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            DispHour.setFormatter(new NumberPicker.Formatter(){
                @Override
                public String format(int i) {
                    return String.format("%d", i);
                }
            });

            // 更新時間ピッカー
            NumberPicker UpdateTime = (NumberPicker)findViewById(R.id.numberPicker2);
            UpdateTime.setMinValue(0);
            UpdateTime.setMaxValue(11);
            UpdateTime.setValue(15);
            UpdateTime.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            UpdateTime.setFormatter(new NumberPicker.Formatter(){
                @Override
                public String format(int i) {
                    return String.format("%02d", i*5);
                }
            });

            SeekBar trans = (SeekBar)findViewById(R.id.seekBar);
            trans.setMax(255);
            trans.setProgress(128);

        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }
}
