package com.mobile.sca;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class AppMessagingService extends FirebaseMessagingService {
    private static final String TAG = "SCA_FCM";
    private static final String CHANNEL_ID = "firebase_messages";

    public static void initialize(Context context) {
        createChannel(context);
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> logToken(context, token))
                .addOnFailureListener(error ->
                        Log.w(TAG, "FCM token retrieval failed: " + error.getMessage()));
    }

    private static void logToken(Context context, String token) {
        // Print tokens for development/testing only.
        if ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Log.d(TAG, "FCM_TOKEN: " + token);
        }
    }

    private static void createChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_ID, "App messages", NotificationManager.IMPORTANCE_DEFAULT));
    }

    @Override
    public void onNewToken(@NonNull String token) {
        logToken(this, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Log.d(TAG, "FCM message received");
        RemoteMessage.Notification notification = message.getNotification();
        String title = notification != null ? notification.getTitle() : message.getData().get("title");
        String body = notification != null ? notification.getBody() : message.getData().get("body");
        if (title == null && body == null) {
            return;
        }
        createChannel(this);
        if ((Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                || !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Log.w(TAG, "Message received, but notification permission is disabled");
            return;
        }
        Intent intent = new Intent(this, SplashActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_push_notification)
                .setContentTitle(title != null ? title : getApplicationInfo().loadLabel(getPackageManager()))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        // Background notification messages are displayed by FCM itself.
        NotificationManagerCompat.from(this).notify(
                "fcm", (int) android.os.SystemClock.uptimeMillis(), builder.build());
    }
}
