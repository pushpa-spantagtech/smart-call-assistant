package com.mobile.sca;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class DndOffReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("DND_TEST", "DND OFF Receiver Fired!");

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm.isNotificationPolicyAccessGranted()) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        }

        showNotification(context);
    }

    private void showNotification(Context context) {
        String channelId = "dnd_channel";
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "DND Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.cropped_circle_image)
                        .setContentTitle("DND Disabled")
                        .setContentText("DND was turned OFF automatically")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        nm.notify(101, builder.build());
    }
}
