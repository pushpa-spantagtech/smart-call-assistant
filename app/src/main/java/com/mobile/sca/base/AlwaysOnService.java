package com.mobile.sca.base;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

public class AlwaysOnService extends Service {

    private static final String CHANNEL_ID = "always_on_channel";
    private static final int NOTIF_ID = 42;
    private final Handler handler = new Handler();
    private final Runnable heartbeat = new Runnable() {
        @Override
        public void run() {
            // TODO: hook your “On/Off schedule” actions here.
            // For now, just re-post the runnable every minute.
            handler.postDelayed(this, 60_000);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("RunningCreate","yes");
        ensureChannel();
    }


    private void setDndEnabled(boolean enabled) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null && nm.isNotificationPolicyAccessGranted()) {
            nm.setInterruptionFilter(
                    enabled ? NotificationManager.INTERRUPTION_FILTER_NONE
                            : NotificationManager.INTERRUPTION_FILTER_ALL
            );
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Running","yes");
        if (intent != null && intent.hasExtra("title")) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            updateNotification(title, message);
            return START_STICKY;
        }
        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Schedule service running")
                .setContentText("Managing On/Off times in background")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();

        // Start as foreground, passing the service type for Android 14+
        int types = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
        ServiceCompat.startForeground(this, NOTIF_ID, notif, types); // androidx.core
        handler.post(heartbeat);

//        if (intent != null && intent.getBooleanExtra("enableDnd", false)) {
//            boolean enable = intent.getBooleanExtra("enableDnd", false);
//            Log.e("Intent", "NOTNUll");
//            setDndEnabled(enable);
//        }
//        else {
//            Log.e("Intent", "NUll");
//        }

        return START_STICKY; // request restart if system kills us
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Always-on background", NotificationManager.IMPORTANCE_MIN);
            ch.setDescription("Runs schedule tasks continuously");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }
    private void updateNotification(String title, String message) {
        Notification updatedNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .build();
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIF_ID, updatedNotification);
    }
}
