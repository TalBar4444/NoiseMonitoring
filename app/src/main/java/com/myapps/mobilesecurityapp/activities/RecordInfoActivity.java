package com.myapps.mobilesecurityapp.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textview.MaterialTextView;
import com.myapps.mobilesecurityapp.R;
import com.myapps.mobilesecurityapp.model.Audio;

public class RecordInfoActivity extends AppCompatActivity {
    private MaterialTextView recordInfo_LBL_back, recordInfo_LBL_minNoiseLevel,
            recordInfo_LBL_maxNoiseLevel, recordInfo_LBL_averageNoiseLevel,
            recordInfo_LBL_date, recordInfo_LBL_duration;
    private Audio recivedAudio;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_info);
        recivedAudio = (Audio)getIntent().getSerializableExtra("KEY_AUDIO");
        findViews();
        initViews();

        recordInfo_LBL_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    private void findViews() {
        recordInfo_LBL_back = findViewById(R.id.recordInfo_LBL_back);
        recordInfo_LBL_minNoiseLevel = findViewById(R.id.recordInfo_LBL_minNoiseLevel);
        recordInfo_LBL_maxNoiseLevel = findViewById(R.id.recordInfo_LBL_maxNoiseLevel);
        recordInfo_LBL_averageNoiseLevel = findViewById(R.id.recordInfo_LBL_averageNoiseLevel);
        recordInfo_LBL_date = findViewById(R.id.recordInfo_LBL_date);
        recordInfo_LBL_duration = findViewById(R.id.recordInfo_LBL_duration);
    }

    private void initViews() {
        String min = String.valueOf(recivedAudio.getMinVolume());
        String max = String.valueOf(recivedAudio.getMaxVolume());
        String average = String.valueOf(recivedAudio.getAverageVolume());
        String duration = String.valueOf(recivedAudio.getDuration());

        recordInfo_LBL_minNoiseLevel.setText(min + " dB");
        recordInfo_LBL_maxNoiseLevel.setText(max+ " dB");
        recordInfo_LBL_averageNoiseLevel.setText(average + " dB");
        recordInfo_LBL_date.setText(recivedAudio.getDateRecorded());
        recordInfo_LBL_duration.setText(duration);
    }

}
