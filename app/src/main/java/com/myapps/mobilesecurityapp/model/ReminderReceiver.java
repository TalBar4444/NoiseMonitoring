package com.myapps.mobilesecurityapp.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.myapps.mobilesecurityapp.R;
import com.myapps.mobilesecurityapp.activities.MainActivity;
import com.myapps.mobilesecurityapp.service.RecordingService;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String NOTIFICATION_CHANNEL_ID = "com.guy.class24a_ands_2.CrashAlert";
    private static final int NOTIFICATION_ID = 357;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (checkToActivateReminder(context)) {
            createNotification(context);
        }
    }

    private boolean checkToActivateReminder(Context context) {
        context = context.getApplicationContext();

        // user press start
        boolean needToRun = MyDB.isNeedToRun(context);
        boolean isServiceRunning = RecordingService.isMyServiceRunning(context);
        if (needToRun &&  !isServiceRunning) {
            return true;
        }
        return false;
    }

    public static void cancelNotification(Context mContext) {
        mContext = mContext.getApplicationContext();
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    void createNotification(Context mContext) {
        mContext = mContext.getApplicationContext();

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                mContext,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);


        NotificationCompat.Builder mBuilder;

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "RECORDING_ALERT_NOTIFICATION_CHANNEL", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(true);
            assert mNotificationManager != null;

            mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        } else {
            mBuilder = new NotificationCompat.Builder(mContext);
        }

        mBuilder.setContentTitle("Unexpected test stop")
                .setContentText("We realized that the test was stopped without an initiated stop. We turn it back on")
                .setSmallIcon(R.drawable.ic_stop)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent);

        assert mNotificationManager != null;
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
