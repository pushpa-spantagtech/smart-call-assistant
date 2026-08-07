package com.mobile.sca.base;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.mobile.sca.AlarmDatabase;
import com.mobile.sca.HomeFrag;

import java.util.HashSet;
import java.util.Set;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private static final int OFF_REQUEST_OFFSET = 1_000_000;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean turnOn = intent.getBooleanExtra("TURN_ON", true);
        int alarmId = intent.getIntExtra("ALARM_ID", -1);
        String title = safeTitle(intent.getStringExtra("TITLE"));
        int dayOfWeek = intent.getIntExtra("DAY_OF_WEEK", -1);
        long durationMs = Math.max(0L, intent.getLongExtra("DURATION_MS", 0L));
        String requestCodeText = intent.getStringExtra("Req");
        int occurrenceRequestCode = parseRequestCode(requestCodeText, alarmId);
        String sessionToken = intent.getStringExtra("SESSION_TOKEN");

        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            sessionToken = buildSessionToken(alarmId, occurrenceRequestCode);
        }

        Log.d(TAG, "turnOn=" + turnOn + ", alarmId=" + alarmId
                + ", requestCode=" + occurrenceRequestCode
                + ", token=" + sessionToken);

        if (turnOn) {
            handleStart(
                    context,
                    alarmId,
                    occurrenceRequestCode,
                    sessionToken,
                    title,
                    dayOfWeek,
                    durationMs
            );
        } else {
            handleEnd(
                    context,
                    alarmId,
                    occurrenceRequestCode,
                    sessionToken,
                    title,
                    dayOfWeek
            );
        }
    }

    private void handleStart(
            Context context,
            int alarmId,
            int occurrenceRequestCode,
            String sessionToken,
            String title,
            int dayOfWeek,
            long durationMs
    ) {
        long endAtMillis = System.currentTimeMillis() + durationMs;

        boolean dndEnabled = DndSessionManager.startSession(
                context,
                sessionToken,
                endAtMillis,
                title
        );

        if (!dndEnabled) {
            Log.e(TAG, "DND was not enabled because Notification Policy access is missing");
            sendStatusNotification(
                    context,
                    "Permission required",
                    "Allow Do Not Disturb access so scheduled meetings can enable DND."
            );
            return;
        }

        if (dayOfWeek != -1) {
            updateRecurringOccurrenceStatus(
                    context,
                    alarmId,
                    occurrenceRequestCode,
                    "Fired"
            );
        } else {
            updateOneTimeStatus(context, alarmId, title, "ACTIVE", "FIRED");
            SharedPreferences statusPrefs =
                    context.getSharedPreferences("status", Context.MODE_PRIVATE);
            statusPrefs.edit()
                    .putInt("alarmId", alarmId)
                    .putLong("durationMs", durationMs)
                    .apply();
        }

        scheduleEndAlarm(
                context,
                alarmId,
                occurrenceRequestCode,
                sessionToken,
                endAtMillis,
                title,
                dayOfWeek
        );

        reloadHome();
        sendStatusNotification(
                context,
                displayTitle(title),
                displayTitle(title) + " meeting has started"
        );
    }

    private void handleEnd(
            Context context,
            int alarmId,
            int occurrenceRequestCode,
            String sessionToken,
            String title,
            int dayOfWeek
    ) {
        if (dayOfWeek != -1) {
            updateRecurringOccurrenceStatus(
                    context,
                    alarmId,
                    occurrenceRequestCode,
                    "Completed"
            );
        } else {
            updateOneTimeStatus(context, alarmId, title, "FIRED", "Completed");
        }

        // DND is restored only when no other meeting session is active.
        boolean restored = DndSessionManager.endSession(context, sessionToken);
        if (!restored && !DndSessionManager.hasPolicyAccess(context)) {
            Log.e(TAG, "Could not restore DND because Notification Policy access is missing");
        }

        reloadHome();
        sendStatusNotification(
                context,
                displayTitle(title),
                displayTitle(title) + " meeting has ended"
        );
    }

    /**
     * Used both during normal scheduling and after a phone reboot.
     */
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleEndAlarm(
            Context context,
            int alarmId,
            int occurrenceRequestCode,
            String sessionToken,
            long endAtMillis,
            String title,
            int dayOfWeek
    ) {
        Intent endIntent = new Intent(context, AlarmReceiver.class);
        endIntent.putExtra("TURN_ON", false);
        endIntent.putExtra("ALARM_ID", alarmId);
        endIntent.putExtra("Req", String.valueOf(occurrenceRequestCode));
        endIntent.putExtra("SESSION_TOKEN", sessionToken);
        endIntent.putExtra("TITLE", title.replace("ACTIVE", "FIRED"));
        endIntent.putExtra("DAY_OF_WEEK", dayOfWeek);

        int endRequestCode = endRequestCode(occurrenceRequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                endRequestCode,
                endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is unavailable");
            return;
        }

        long triggerAt = Math.max(System.currentTimeMillis() + 1000L, endAtMillis);
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
        );
    }

    private static int endRequestCode(int occurrenceRequestCode) {
        long candidate = (long) occurrenceRequestCode + OFF_REQUEST_OFFSET;
        if (candidate > Integer.MAX_VALUE) {
            candidate = Math.abs((long) occurrenceRequestCode * 31L + 17L);
        }
        return (int) candidate;
    }

    private static String buildSessionToken(int alarmId, int occurrenceRequestCode) {
        return "alarm_" + alarmId + "_occurrence_" + occurrenceRequestCode;
    }

    private static int parseRequestCode(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static void updateRecurringOccurrenceStatus(
            Context context,
            int alarmId,
            int requestCode,
            String newStatus
    ) {
        SharedPreferences prefs =
                context.getSharedPreferences(String.valueOf(alarmId), Context.MODE_PRIVATE);
        Set<String> saved =
                new HashSet<>(prefs.getStringSet("KEY_REQUEST_CODES", new HashSet<>()));
        Set<String> updated = new HashSet<>();
        boolean found = false;

        for (String item : saved) {
            String[] parts = item.split("::", 2);
            if (parts.length > 0 && parts[0].equals(String.valueOf(requestCode))) {
                updated.add(requestCode + "::" + newStatus);
                found = true;
            } else {
                updated.add(item);
            }
        }

        if (!found) {
            updated.add(requestCode + "::" + newStatus);
        }

        prefs.edit().putStringSet("KEY_REQUEST_CODES", updated).apply();
    }

    private static void updateOneTimeStatus(
            Context context,
            int alarmId,
            String title,
            String oldStatus,
            String newStatus
    ) {
        try {
            String updatedTitle = title.contains(oldStatus)
                    ? title.replace(oldStatus, newStatus)
                    : title;
            AlarmDatabase.getInstance(context)
                    .alarmDao()
                    .updateAlarmTitle(alarmId, updatedTitle);
        } catch (Exception e) {
            Log.e(TAG, "Unable to update alarm status", e);
        }
    }

    private static void reloadHome() {
        try {
            if (HomeFrag.ins != null) {
                HomeFrag.ins.reload();
            }
        } catch (Exception e) {
            Log.w(TAG, "Home screen could not be refreshed", e);
        }
    }

    private static void sendStatusNotification(Context context, String title, String message) {
        try {
            Intent updateIntent = new Intent(context, AlwaysOnService.class);
            updateIntent.putExtra("title", title);
            updateIntent.putExtra("message", message);
            context.startService(updateIntent);
        } catch (Exception e) {
            Log.w(TAG, "Status service could not be started", e);
        }
    }

    private static String safeTitle(String title) {
        return title == null || title.trim().isEmpty() ? "Meeting::ACTIVE" : title;
    }

    private static String displayTitle(String title) {
        String[] parts = title.split("::", 2);
        return parts.length == 0 || parts[0].trim().isEmpty() ? "Meeting" : parts[0];
    }
}
