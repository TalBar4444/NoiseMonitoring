package com.myapps.mobilesecurityapp.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.myapps.mobilesecurityapp.R;
import com.myapps.mobilesecurityapp.activities.MainActivity;
import com.myapps.mobilesecurityapp.model.Audio;
import com.myapps.mobilesecurityapp.model.AudioList;
import com.myapps.mobilesecurityapp.model.MSPV;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class RecordingService extends Service {
    private final static String LOG_TAG = "pttt";
    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";
    public static final String BROADCAST_NOISE_LEVEL = "BROADCAST_NOISE_LEVEL";
    public static final String BROADCAST_NOISE_LEVEL_KEY = "BROADCAST_NOISE_LEVEL_KEY";
    public static int NOTIFICATION_ID = 168;
    private int lastShownNotificationId = -1;
    public static String CHANNEL_ID = "com.myapps.mobilesecurityapp.CHANNEL_ID_FOREGROUND";
    public static String MAIN_ACTION = "com.myapps.mobilesecurityapp.recordingservice.action.main";
    private NotificationCompat.Builder notificationBuilder;
    private static final int POLL_INTERVAL = 100; // milliseconds
    private static final double FULL_SCALE_VALUE = 32767.0;
    private Handler handler = new Handler();

    private LocalDate today = LocalDate.now();

    private AudioList audioList = MSPV.getMe().readAudios();
    private long startTime,endTime;
    private long totalAmplitude = 0;
    private int numSamples = 0;
    private double maxNoiseLevel = 0, minNoiseLevel = -1;
    private boolean isServiceRunningRightNow = false;
    private MediaRecorder recorder;
    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null){
            Log.d(LOG_TAG ,"intent == null");
            stopForeground(true);
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if(action.equals(START_FOREGROUND_SERVICE)){
            if (isServiceRunningRightNow) {
                return START_STICKY;
            }
            isServiceRunningRightNow = true;
            notifyToUserForForegroundService();
            startRecording(); // Start recording on service start
        } else if (action.equals(STOP_FOREGROUND_SERVICE)) {
            stopRecording();
            stopForeground(true);
            stopSelf();
            isServiceRunningRightNow = false;
        }
        return START_STICKY;
    }

    final Runnable updater = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, POLL_INTERVAL);
            if(recorder != null){
                int amplitude = 0;

                try {
                    amplitude = recorder.getMaxAmplitude();
                    double normalizedAmplitude = Math.abs((double) amplitude) / FULL_SCALE_VALUE; // Replace with full scale value for your format

                    double soundLevel = (normalizedAmplitude * 100); // Assuming full scale maps to 100
                    Log.d(LOG_TAG,"Noise level + " + soundLevel);

                    checkAmplitudes(soundLevel);
                    totalAmplitude += soundLevel;
                    numSamples++;

                    Intent intentNoiseLevel = new Intent(BROADCAST_NOISE_LEVEL);
                    intentNoiseLevel.putExtra(BROADCAST_NOISE_LEVEL_KEY, String.valueOf((int) soundLevel));
                    LocalBroadcastManager.getInstance(RecordingService.this).sendBroadcast(intentNoiseLevel);

                } catch (Exception e){
                    Log.d(LOG_TAG,"Runnable failed");
                }

            }
        }
    };

    private void startRecording() {
        resetData();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){ // && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            File file = null;
            try {
                file = File.createTempFile("prefix", ".extension", getApplicationContext().getCacheDir());
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    recorder.setOutputFile(file);
                }
            }
            catch (IOException e){
                Log.d(LOG_TAG, "create file failed");
            }

            try {
                recorder.prepare();;
            }
            catch (Exception e){
                Log.d(LOG_TAG, "prepare() failed");
            }
            recorder.start();
            handler.post(updater); // start the thread
            startTime = System.currentTimeMillis();
        }
    }

    private void stopRecording() {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PassiveApp:tag");
        wakeLock.acquire();

        if (handler != null && updater != null) {
            handler.removeCallbacks(updater); // stops the thread
        }
        if (recorder != null) {
            try{
                recorder.stop();
                recorder.release();
                endTime = System.currentTimeMillis();
            } catch (Exception e){
                Log.e(LOG_TAG, "stopRecording() failed");
            } finally {
                recorder = null;
            }
        }
        addNewNoiseRecorder();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runs = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RecordingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(recorder != null){
            try{
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
            }
            catch (Exception e){
                Log.e(LOG_TAG, "onDestroy() failed");
            }
        }
        if (handler != null) {
            handler.removeCallbacks(updater);
        }
    }

    ////////////////////////////////////////// Notification ////////////////////////////////////////


    private void notifyToUserForForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = getNotificationBuilder(this,
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        notificationBuilder
                .setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_mic)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle("App in progress")
                .setContentText("Content");

        Notification notification = notificationBuilder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        }

        if (NOTIFICATION_ID != lastShownNotificationId) { //for 2 services - cancel the previous notification, protection for the notification
            // Cancel previous notification
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "Recording app record channel";
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(notifications_channel_description);

                // from another answer
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

    private void updateNotification(String content) {
        notificationBuilder.setContentText(content);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    ////////////////////////////////////// important functions /////////////////////////////////

    private void checkAmplitudes(double soundLevel) {
        /////////// minimum ////////////////////

        if(minNoiseLevel == -1){
            minNoiseLevel = soundLevel;
        }
        else{
            if(soundLevel < minNoiseLevel)
                minNoiseLevel = soundLevel;
        }
        //////// maximum //////////////
        if(soundLevel > maxNoiseLevel)
            maxNoiseLevel = soundLevel;
    }

    private void resetData() {
        numSamples = 0;
        totalAmplitude = 0;
        minNoiseLevel = -1;
        maxNoiseLevel = 0;
    }

    private void addNewNoiseRecorder() {
        if (numSamples > 0) {
            int myMinNoiseLevel = (int) minNoiseLevel;
            int myMaxNoiseLevel = (int) maxNoiseLevel;
            int myAverageNoiseLevel = (int) (totalAmplitude / numSamples);
            long durationInMilliSec = endTime - startTime; // duration in milliseconds

            String date = today.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            Audio audio = new Audio()
                    .setGenericName()
                    .setMinVolume(myMinNoiseLevel)
                    .setMaxVolume(myMaxNoiseLevel)
                    .setAverageVolume(myAverageNoiseLevel)
                    .setDateRecorded(date)
                    .setDuration(durationInMilliSec);
            try {
                audioList.addAudio(audio);

            } catch (Exception e) {
                Log.d(LOG_TAG, "add to audioList failed()");
            }


            if(audioList != null) {
                try {
                    MSPV.getMe().saveAudios(audioList);
                }catch (Exception e){
                    Log.d(LOG_TAG,"audioList size failed");
                }
            }
        }
    }
}
