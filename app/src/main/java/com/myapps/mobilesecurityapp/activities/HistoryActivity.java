package com.myapps.mobilesecurityapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.myapps.mobilesecurityapp.R;
import com.myapps.mobilesecurityapp.adapters.AudioAdapter;
import com.myapps.mobilesecurityapp.interfaces.Callback_Item;
import com.myapps.mobilesecurityapp.interfaces.RecyclerViewInterface;
import com.myapps.mobilesecurityapp.model.Audio;
import com.myapps.mobilesecurityapp.model.AudioList;
import com.myapps.mobilesecurityapp.model.MSPV;

import java.util.List;

public class HistoryActivity extends AppCompatActivity implements RecyclerViewInterface {
    private MaterialTextView history_LBL_back, history_LBL_noRecords;
    private RecyclerView history_LST_records;
    AudioList audioList = MSPV.getMe().readAudios();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        findViews();
        initList();

        history_LBL_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    private void findViews() {
        history_LBL_back = findViewById(R.id.history_LBL_back);
        history_LBL_noRecords = findViewById(R.id.history_LBL_noRecords);
        history_LST_records = findViewById(R.id.history_LST_records);
    }

    private void initList(){
        if(audioList != null && audioList.getSize() > 0){
            history_LBL_noRecords.setVisibility(View.GONE);
            AudioAdapter audioAdapter = new AudioAdapter(getAllAudios(),this);

            history_LST_records.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));
            history_LST_records.setHasFixedSize(true);
            history_LST_records.setAdapter(audioAdapter);
        }
        else{
            history_LBL_noRecords.setVisibility(View.VISIBLE);
        }
    }

    private Callback_Item callbackItem = new Callback_Item() {
        @Override
        public void openRecordInfo(Audio audio) {
            openRecordInfoActivity(audio);
        }
    };

    @Override
    public void onItemClick(int pos) {
        callbackItem.openRecordInfo(getAllAudios().get(pos));
    }

    public List<Audio> getAllAudios() {
        List<Audio> allAudios = audioList.getAudios();
        return allAudios;
    }
    private void openRecordInfoActivity(Audio audio) {
        Intent intent = new Intent(this, RecordInfoActivity.class);
        intent.putExtra("KEY_AUDIO", audio);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
