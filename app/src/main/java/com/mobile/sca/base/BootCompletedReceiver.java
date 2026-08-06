package com.mobile.sca.base;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.mobile.sca.AlarmEntity;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class BootCompletedReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Toast.makeText(context, "Boot Completed!!", Toast.LENGTH_SHORT).show();
            // Fetch ACTIVE alarms from DB / SharedPref
//            List<AlarmEntity> activeAlarms =
//                    AlarmDatabase.getInstance(context)
//                            .alarmDao()
//                            .getAllAlarms();
//
//            for (AlarmEntity alarm : activeAlarms) {
//               // AlarmScheduler.schedule(context, alarm);
//                saveAlarm(alarm);
//            }
        }
    }

    private void saveAlarm(AlarmEntity alarm) {

        if (alarm.endday != 0) {
            //scheduleWeeklyAlarmsWithDate(context, alarm);
            TimeUtils.scheduleWeeklyAlarmsWithDate(context, alarm);
        } else if (alarm.days.isEmpty()) {
            if (alarm.title.contains("ACTIVE")) {
                TimeUtils.scheduleOneTimeAlarm(context, alarm);
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleWeeklyAlarmsWithDate(Context context, AlarmEntity alarm) {
        SharedPreferences prefs =
                context.getSharedPreferences("" + alarm.id, Context.MODE_PRIVATE);

        Set<String> savedSet =
                prefs.getStringSet("KEY_REQUEST_CODES", new HashSet<>());
        for (String item : savedSet) {
            Log.e("AdapterItem", item);
            if (item.contains("ACTIVE")) {

                Calendar startCal = Calendar.getInstance();
                startCal.set(Calendar.YEAR, alarm.year);
                startCal.set(Calendar.MONTH, alarm.month);
                startCal.set(Calendar.DAY_OF_MONTH, alarm.day);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);

                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra("ALARM_ID", alarm.id);
                intent.putExtra("TURN_ON", true);
                intent.putExtra("DURATION_MS", alarm.duration * 60L * 1000L);
                intent.putExtra("DAY_OF_WEEK", "1");
                intent.putExtra("TITLE", alarm.title);

                int requestCode = Integer.parseInt(item.split("::")[0]);

                intent.putExtra("Req", item.split("::")[0]);

                PendingIntent pi = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        startCal.getTimeInMillis(),
                        pi
                );
            }
        }
    }

}
