package com.myapps.mobilesecurityapp.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.myapps.mobilesecurityapp.model.MyDB;
import com.myapps.mobilesecurityapp.model.MyReminder;
import com.myapps.mobilesecurityapp.R;
import com.myapps.mobilesecurityapp.service.RecordingService;
import com.myapps.mobilesecurityapp.model.ScreenVisualization;

public class MainActivity extends AppCompatActivity {
    private MaterialButton main_BTN_start, main_BTN_history;
    private MaterialTextView main_LBL_noiseLevel;
    private MaterialCardView main_CARD_recordingOff;
    private LinearLayoutCompat main_LL_recordingON;
    private final static String LOG_TAG = "pttt";
    public static ScreenVisualization screenVisualization;
    private boolean mStartRecording = true;
    private boolean isServiceRunning = false;
    private boolean permissionsGranted = false;
    private boolean permissionToRecord = false;

    private BroadcastReceiver recordingBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String noiseLevel = intent.getStringExtra(RecordingService.BROADCAST_NOISE_LEVEL_KEY);
            int maxAmplitude =0;
            if(noiseLevel != null && screenVisualization != null) {
                try {
                    main_LBL_noiseLevel.setText(noiseLevel);
                    maxAmplitude = Integer.parseInt(noiseLevel);
                    Log.d("myAudio"," max amplitude " + maxAmplitude);
                    if (maxAmplitude > 0)
                        screenVisualization.addAmplitude(maxAmplitude);
                } catch (Exception exception) {
                    Log.d(LOG_TAG,"Error in receiver");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();

        permissionsGranted = isSelfPermissionsGranted();
        main_BTN_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordButtonView();
            }
        });

        main_BTN_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHistoryActivity();
            }
        });

        MyReminder.startReminder(this);
        isServiceRunning = RecordingService.isMyServiceRunning(MainActivity.this);
        if(isServiceRunning){
            recordButtonView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilterNoiseLevel = new IntentFilter(RecordingService.BROADCAST_NOISE_LEVEL);
        LocalBroadcastManager.getInstance(this).registerReceiver(recordingBroadcastReceiver, intentFilterNoiseLevel);
        start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(recordingBroadcastReceiver);
    }

    private void recordButtonView() {
        if (mStartRecording){
            main_BTN_start.setText(R.string.stop_recording);
            main_LL_recordingON.setVisibility(View.VISIBLE);
            main_CARD_recordingOff.setVisibility(View.GONE);
        } else {
            main_BTN_start.setText(R.string.start_recording);
            main_LL_recordingON.setVisibility(View.GONE);
            main_CARD_recordingOff.setVisibility(View.VISIBLE);
        }
        handleRecord(mStartRecording);
        mStartRecording = !mStartRecording;
    }

    private void handleRecord(boolean start) {
        if (start) {
            MyDB.saveState(this, true);
            sendActionToService(RecordingService.START_FOREGROUND_SERVICE);
        } else {
            sendActionToService(RecordingService.STOP_FOREGROUND_SERVICE);
        }
    }

    private void sendActionToService(String action) {
        Intent intent = new Intent(this, RecordingService.class);
        intent.setAction(action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public boolean isSelfPermissionsGranted(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void start() {
        permissionToRecord = isSelfPermissionsGranted();
        updateUI(permissionToRecord);
    }

    private void updateUI(Boolean permissionToRecord) {
        if (!permissionToRecord) {
            Log.d(LOG_TAG," in updateUI");
            main_BTN_start.setClickable(false);
            askForPermissions(checkForMissingPermission(this));
        } else{
            main_BTN_start.setClickable(true);
        }
    }

    private static String checkForMissingPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return Manifest.permission.RECORD_AUDIO;
        }
        return null;
    }

    private void askForPermissions(String permission) {
        if (shouldShowRequestPermissionRationale(permission)) {
            if (permission.equals(Manifest.permission.RECORD_AUDIO)  &&  Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                buildAlertMessageManuallyBackgroundPermission(permission);
            } else {
                requestPermissionLauncher.launch(permission);
            }

        } else {
            // 1. First Time
            // 2. Don't Ask Me Again state.
            requestPermissionLauncher.launch(permission);
        }
    }

    private void buildAlertMessageManuallyBackgroundPermission(String permission) {
        if (permission == null) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String suffix = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? "Allow only while using the app" : "Allow";

        builder.setTitle("Microphone")
                .setMessage("It look like you banned the access to your microphone" +
                        "\nClick on Settings, then check  '" + suffix + "'")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        openAppSettings();                    }
                })
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        main_BTN_start.setClickable(false);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            start();
                        } else {

                            if (shouldShowRequestPermissionRationale(checkForMissingPermission(this))) {
                                Snackbar.make(findViewById(android.R.id.content),
                                                R.string.permission_rationale,
                                                Snackbar.LENGTH_INDEFINITE)
                                        .setDuration(Snackbar.LENGTH_LONG)
                                        .setAction(R.string.settings, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                requestPermissionLauncher.launch(checkForMissingPermission(MainActivity.this));
                                            }
                                        })
                                        .show();
                            } else {
                                buildAlertMessageManuallyBackgroundPermission(checkForMissingPermission(this));
                            }
                        }
                    }
            );

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appSettingsResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> appSettingsResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                start();
            }
        }
    );

    private void findViews() {
        main_BTN_start = findViewById(R.id.main_BTN_start);
        main_BTN_history = findViewById(R.id.main_BTN_history);
        main_LBL_noiseLevel = findViewById(R.id.main_LBL_noiseLevel);
        main_CARD_recordingOff = findViewById(R.id.main_CARD_recordingOff);
        main_LL_recordingON = findViewById(R.id.main_LL_recordingON);
        screenVisualization = findViewById(R.id.visualization);
    }

    private void openHistoryActivity(){
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}