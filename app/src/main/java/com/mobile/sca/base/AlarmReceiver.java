package com.mobile.sca.base;

import static android.content.Context.MODE_PRIVATE;

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

    SharedPreferences pref;

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean turnOn = intent.getBooleanExtra("TURN_ON", true);
        pref = context.getSharedPreferences("status", MODE_PRIVATE);

        int alarmId = intent.getIntExtra("ALARM_ID", -1);
        String title = intent.getStringExtra("TITLE");
        int dayOfWeek = intent.getIntExtra("DAY_OF_WEEK", -1);
        long durationMs = intent.getLongExtra("DURATION_MS", 0);

        String Req = intent.getStringExtra("Req");
        Log.e("dayOfWeek", "" + dayOfWeek);

        if (dayOfWeek != -1) {
            if (turnOn) {
                DndUtils.setDnd(context, true);
                SharedPreferences prefs =
                        context.getSharedPreferences("" + alarmId, Context.MODE_PRIVATE);
                Set<String> savedSet =
                        prefs.getStringSet("KEY_REQUEST_CODES", new HashSet<>());
                Set<String> updatedSet = new HashSet<>();
                for (String item : savedSet) {
                    String[] parts = item.split("::");
                    int rc = Integer.parseInt(parts[0]);
                    Log.e("ItemInReceiver", item + " " + Req);
                    if (("" + rc).equals("" + Req)) {
                        updatedSet.add(rc + "::" + "Fired");
                    } else {
                        updatedSet.add(item);
                    }
                }
                prefs.edit()
                        .putStringSet("KEY_REQUEST_CODES", updatedSet)
                        .apply();
                assert Req != null;
                assert title != null;
                scheduleDndOffWeekly(alarmId, Req, context, Integer.parseInt(Req), durationMs, title);
                try {
                    HomeFrag.ins.reload();
                } catch (Exception e) {

                }
                try {
                    Intent updateIntent = new Intent(context, AlwaysOnService.class);
                    updateIntent.putExtra("title", title.split("::")[0]);
                    updateIntent.putExtra("message", title.split("::")[0] + " meeting is started");

                    context.startService(updateIntent);
                } catch (Exception e) {
                }
            } else {
                SharedPreferences prefs =
                        context.getSharedPreferences("" + alarmId, Context.MODE_PRIVATE);
                Set<String> savedSet =
                        prefs.getStringSet("KEY_REQUEST_CODES", new HashSet<>());
                Set<String> updatedSet = new HashSet<>();
                for (String item : savedSet) {
                    String[] parts = item.split("::");
                    int rc = Integer.parseInt(parts[0]);
                    Log.e("ItemInReceiver", item + " " + Req);
                    if (("" + rc).equals("" + Req)) {
                        updatedSet.add(rc + "::" + "Completed");
                    } else {
                        updatedSet.add(item);
                    }
                }
                prefs.edit()
                        .putStringSet("KEY_REQUEST_CODES", updatedSet)
                        .apply();
                DndUtils.setDnd(context, false);
                try {
                    HomeFrag.ins.reload();
                } catch (Exception ignored) {
                }
                try {
                    Intent updateIntent = new Intent(context, AlwaysOnService.class);
                    updateIntent.putExtra("title", title.split("::")[0]);
                    updateIntent.putExtra("message", title.split("::")[0] + " meeting is ended now");

                    context.startService(updateIntent);
                } catch (Exception e) {
                }
            }
        } else {
            if (turnOn) {

                // 🔕 Turn ON DND
                DndUtils.setDnd(context, true);
                assert title != null;
                AlarmDatabase.getInstance(context)
                        .alarmDao()
                        .updateAlarmTitle(alarmId, title.replace("ACTIVE", "FIRED"));


                pref.edit().putInt("alarmId", alarmId).commit();
                pref.edit().putLong("durationMs", durationMs).commit();
                Log.e("TurnONRequest", "" + alarmId);
                scheduleDndOff(context, alarmId, durationMs, title);
                HomeFrag.ins.reload();
                try {
                    Intent updateIntent = new Intent(context, AlwaysOnService.class);
                    updateIntent.putExtra("title", title.split("::")[0]);
                    updateIntent.putExtra("message", title.split("::")[0] + " meeting is started");

                    context.startService(updateIntent);
                } catch (Exception e) {
                }
            } else {
                Log.e("TurnOffRequest", "" + alarmId + " " + title);
                assert title != null;
                if (title.contains("FIRED")) {
                    AlarmDatabase.getInstance(context)
                            .alarmDao()
                            .updateAlarmTitle((alarmId - 999), title.replace("FIRED", "Completed"));
                }
                // 🔔 Turn OFF DND
                DndUtils.setDnd(context, false);
                try {
                    Intent updateIntent = new Intent(context, AlwaysOnService.class);
                    updateIntent.putExtra("title", title.split("::")[0]);
                    updateIntent.putExtra("message", title.split("::")[0] + " meeting is ended now");

                    context.startService(updateIntent);
                } catch (Exception e) {
                }
                try {
                    HomeFrag.ins.reload();
                } catch (Exception e) {
                }
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleDndOff(Context context, int alarmId, long durationMs, String title) {

        long triggerAt = System.currentTimeMillis() + durationMs;
        Log.e("TurnOffRequestInitiated", "" + alarmId);
        Log.e("NewCOde", "" + (alarmId + 999));


        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("TURN_ON", false);
        intent.putExtra("TITLE", title.replace("ACTIVE", "FIRED"));
        intent.putExtra("ALARM_ID", (alarmId + 999));

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                (alarmId + 999),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pi
        );
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleDndOffWeekly(int id, String Req1, Context context, int Req, long durationMs, String title) {

        long triggerAt = System.currentTimeMillis() + durationMs;


        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("TURN_ON", false);
        intent.putExtra("TITLE", title.replace("ACTIVE", "FIRED"));
        intent.putExtra("DAY_OF_WEEK", 0);
        intent.putExtra("Req", "" + (Req));
        intent.putExtra("ALARM_ID", id);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                (Req),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pi
        );
    }
}
