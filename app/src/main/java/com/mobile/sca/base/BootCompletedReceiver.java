package com.mobile.sca.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mobile.sca.AlarmDatabase;
import com.mobile.sca.AlarmEntity;

import java.util.List;
import java.util.Set;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        final PendingResult pendingResult = goAsync();
        final Context appContext = context.getApplicationContext();

        new Thread(() -> {
            try {
                restoreActiveDndSessions(appContext);
                restoreFutureMeetingAlarms(appContext);
            } catch (Exception e) {
                Log.e(TAG, "Failed to restore alarms after reboot/update", e);
            } finally {
                pendingResult.finish();
            }
        }, "sca-alarm-restore").start();
    }

    private void restoreActiveDndSessions(Context context) {
        Set<DndSessionManager.SessionInfo> sessions =
                DndSessionManager.reconcileAfterBoot(context);

        for (DndSessionManager.SessionInfo session : sessions) {
            ParsedToken parsed = ParsedToken.from(session.token);
            AlarmReceiver.scheduleEndAlarm(
                    context,
                    parsed.alarmId,
                    parsed.occurrenceRequestCode,
                    session.token,
                    session.endAtMillis,
                    session.title,
                    parsed.isRecurring ? 1 : -1
            );
        }
    }

    private void restoreFutureMeetingAlarms(Context context) {
        List<AlarmEntity> alarms = AlarmDatabase.getInstance(context)
                .alarmDao()
                .getAllAlarms();

        long now = System.currentTimeMillis();
        for (AlarmEntity alarm : alarms) {
            if (alarm == null || alarm.title == null) continue;
            if (alarm.title.contains("Completed") || alarm.title.contains("CANCELLED")) continue;

            if (alarm.endday != 0 && alarm.days != null && !alarm.days.trim().isEmpty()) {
                TimeUtils.scheduleWeeklyAlarmsWithDate(context, alarm);
            } else if ((alarm.days == null || alarm.days.trim().isEmpty())
                    && alarm.title.contains("ACTIVE")
                    && TimeUtils.getOneTimeTriggerMillis(alarm) > now) {
                TimeUtils.scheduleOneTimeAlarm(context, alarm);
            }
        }
    }

    private static final class ParsedToken {
        final int alarmId;
        final int occurrenceRequestCode;
        final boolean isRecurring;

        private ParsedToken(int alarmId, int occurrenceRequestCode, boolean isRecurring) {
            this.alarmId = alarmId;
            this.occurrenceRequestCode = occurrenceRequestCode;
            this.isRecurring = isRecurring;
        }

        static ParsedToken from(String token) {
            try {
                String[] parts = token.split("_");
                int alarmId = Integer.parseInt(parts[1]);
                int occurrence = Integer.parseInt(parts[3]);
                return new ParsedToken(alarmId, occurrence, alarmId != occurrence);
            } catch (Exception ignored) {
                int fallback = token == null ? 0 : token.hashCode();
                return new ParsedToken(fallback, fallback, false);
            }
        }
    }
}
