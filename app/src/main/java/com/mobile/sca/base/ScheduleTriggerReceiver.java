package com.mobile.sca.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;


public class ScheduleTriggerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean enableDnd = intent.getBooleanExtra("enableDnd", false);

        // Toggle DND immediately
        android.app.NotificationManager nm = context.getSystemService(android.app.NotificationManager.class);
        if (nm != null && nm.isNotificationPolicyAccessGranted()) {
            nm.setInterruptionFilter(enableDnd
                    ? android.app.NotificationManager.INTERRUPTION_FILTER_NONE
                    : android.app.NotificationManager.INTERRUPTION_FILTER_ALL);
        }

        // Optional: refresh foreground service notification (safe if app is allowed)
        Intent svc = new Intent(context, AlwaysOnService.class);
        svc.putExtra("enableDnd", enableDnd);
        try {
            ContextCompat.startForegroundService(context, svc);
        } catch (Exception e) {
            android.util.Log.w("ScheduleTriggerReceiver", "FGS start blocked: " + e);
        }
    }
}

