package com.myapps.mobilesecurityapp.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.myapps.mobilesecurityapp.R;
import com.myapps.mobilesecurityapp.model.MSPV;


public class StartActivity extends AppCompatActivity {

    private MaterialButton start_BTN_accept;
    private MaterialTextView start_LBL_termsOfUse, start_LBL_privacyPolicy;
    public static final int REQUEST_CODE_PERMISSIONS = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        findViews();
        boolean onPolicyAccepted = MSPV.getMe().readPrivacyPolicy();
        if(onPolicyAccepted)
            openMainActivity();

        start_BTN_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MSPV.getMe().savePrivacyPolicy(true);
                handlePermissions();
            }
        });

        start_LBL_termsOfUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_of_use)));
                startActivity(intent);
            }
        });
        start_LBL_privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy)));
                startActivity(intent);
            }

        });
    }

    private void handlePermissions() {
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO}; // Replace with your permissions

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //openMainActivity();
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0){
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                //MSPV.getMe().savePrivacyPolicy(false);
                Toast.makeText(this, "Permissions not granted!", Toast.LENGTH_SHORT).show();
            }
        }
        openMainActivity();
    }
//
//    private boolean allPermissionsGranted(int[] grantResults) {
//        for (int result : grantResults) {
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }


//    private void handlePermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.RECORD_AUDIO)) {
//                Toast.makeText(this, " Please give permission for audio", Toast.LENGTH_SHORT).show();
//            }
//
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO);
//        }
//    }

    private void findViews() {
        start_BTN_accept = findViewById(R.id.start_BTN_accept);
        start_LBL_termsOfUse = findViewById(R.id.start_LBL_termsOfUse);
        start_LBL_privacyPolicy = findViewById(R.id.start_LBL_privacyPolicy);

    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        Log.d("pttt","from start");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);

    }
}
