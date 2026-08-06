package com.mobile.sca.base;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.mobile.sca.AlarmDatabase;
import com.mobile.sca.AlarmEntity;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class TimeUtils {
    /**
     * Next occurrence of today’s time (or tomorrow if time already passed) in local time zone
     */
    public static long nextTimeMillis(int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Calendar target = (Calendar) c.clone();
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);

        if (target.getTimeInMillis() <= c.getTimeInMillis()) {
            target.add(Calendar.DAY_OF_YEAR, 1); // schedule for tomorrow if already passed
        }
        return target.getTimeInMillis();
    }

    public static void cancelWeeklyAlarms(Context context, AlarmEntity alarm) {

        if (alarm.days == null || alarm.days.isEmpty()) return;

        String[] days = alarm.days.split(",");

        AlarmManager am =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (String day : days) {

            int dayOfWeek = getDayOfWeek(day);
            int requestCode = alarm.id * 10 + dayOfWeek;

            Intent intent = new Intent(context, AlarmReceiver.class);

            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            am.cancel(pi);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleOneTimeAlarm(Context context, AlarmEntity alarm) {

        Log.e("OneTIme", "Yes ONe TIME");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, alarm.year);
        cal.set(Calendar.MONTH, alarm.month);
        cal.set(Calendar.DAY_OF_MONTH, alarm.day);
        cal.set(Calendar.HOUR_OF_DAY, to24Hour(alarm.hour, alarm.amPm));
        cal.set(Calendar.MINUTE, alarm.minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);


        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ALARM_ID", alarm.id);
        intent.putExtra("DURATION_MS", alarm.duration * 60L * 1000L);
        intent.putExtra("TURN_ON", true);
        intent.putExtra("TITLE", alarm.title);
        intent.putExtra("Req", "" + alarm.id);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                alarm.id, // unique per alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                pi
        );

    }

    public static int to24Hour(int hour, String amPm) {
        if ("AM".equals(amPm)) {
            return hour == 12 ? 0 : hour;
        } else {
            return hour == 12 ? 12 : hour + 12;
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleWeeklyAlarms(Context context, AlarmEntity alarm) {

        String[] days = alarm.days.split(",");

        for (String day : days) {

            Calendar cal = Calendar.getInstance();
            int hour24 = to24Hour(alarm.hour, alarm.amPm);

            cal.set(Calendar.HOUR_OF_DAY, hour24);
            cal.set(Calendar.MINUTE, alarm.minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            int targetDay = getDayOfWeek(day);
            int today = cal.get(Calendar.DAY_OF_WEEK);

            int diff = targetDay - today;
            if (diff < 0) diff += 7;

            if (diff == 0 && cal.before(Calendar.getInstance())) {
                diff = 7;
            }

            cal.add(Calendar.DAY_OF_YEAR, diff);

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("ALARM_ID", alarm.id);
            intent.putExtra("TURN_ON", true);
            intent.putExtra("DURATION_MS", alarm.duration * 60L * 1000L);
            intent.putExtra("DAY_OF_WEEK", targetDay);
            intent.putExtra("HOUR_24", hour24);     // ✅ REQUIRED
            intent.putExtra("MINUTE", alarm.minute); // ✅ REQUIRED

            int requestCode = alarm.id * 10 + targetDay;

            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager am =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(),
                    pi
            );
            Log.e("AlarmSetWithWeekDays", "Yes");
        }
    }

    public static int getDayOfWeek(String day) {
        switch (day) {
            case "Mon":
                return Calendar.MONDAY;
            case "Tue":
                return Calendar.TUESDAY;
            case "Wed":
                return Calendar.WEDNESDAY;
            case "Thu":
                return Calendar.THURSDAY;
            case "Fri":
                return Calendar.FRIDAY;
            case "Sat":
                return Calendar.SATURDAY;
            default:
                return Calendar.SUNDAY;
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleWeeklyAlarmsWithDate(Context context, AlarmEntity alarm) {
        String[] days = alarm.days.trim().split(",");
        SharedPreferences prefs =
                context.getSharedPreferences("" + alarm.id, Context.MODE_PRIVATE);

        Set<String> requestCodeSet =
                new HashSet<>(prefs.getStringSet("KEY_REQUEST_CODES", new HashSet<>()));
        // -------- START DATE --------
        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.YEAR, alarm.year);
        startCal.set(Calendar.MONTH, alarm.month);
        startCal.set(Calendar.DAY_OF_MONTH, alarm.day);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        // -------- END DATE --------
        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.YEAR, alarm.endyear);
        endCal.set(Calendar.MONTH, alarm.endmonth);
        endCal.set(Calendar.DAY_OF_MONTH, alarm.endday);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        int hour24 = to24Hour(alarm.hour, alarm.amPm);

        for (String day : days) {
            Calendar cal = (Calendar) startCal.clone();

            Calendar now = Calendar.getInstance();

            cal.set(Calendar.HOUR_OF_DAY, hour24);
            cal.set(Calendar.MINUTE, alarm.minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            int targetDay = getDayOfWeek(day);
            int startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            int diff = targetDay - startDayOfWeek;
            if (diff < 0) diff += 7;

            cal.add(Calendar.DAY_OF_YEAR, diff);
            // -------- SCHEDULE UNTIL END DATE --------
            while (!cal.after(endCal)) {

                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra("ALARM_ID", alarm.id);
                intent.putExtra("TURN_ON", true);
                intent.putExtra("DURATION_MS", alarm.duration * 60L * 1000L);
                intent.putExtra("DAY_OF_WEEK", targetDay);
                intent.putExtra("TITLE", alarm.title);

                int requestCode =
                        alarm.id * 100
                                + targetDay * 100
                                + cal.get(Calendar.WEEK_OF_YEAR);

                intent.putExtra("Req", "" + requestCode);

                PendingIntent pi = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (cal.getTimeInMillis() > now.getTimeInMillis()) {
                    Log.e("Alarm Date..>>>.....", "" + cal.getTime());
                    Log.e("requestCode", "" + requestCode);
                    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    am.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            cal.getTimeInMillis(),
                            pi
                    );
                    requestCodeSet.add(requestCode + "::" + "ACTIVE");
                }
                cal.add(Calendar.WEEK_OF_YEAR, 1);
            }
        }
        prefs.edit()
                .putStringSet("KEY_REQUEST_CODES", requestCodeSet)
                .apply();
    }

    public static void cancelWeeklyAlarmsWithDate(Context context, AlarmEntity alarm) {

        String[] days = alarm.days.trim().split(",");
        Log.e("ALRAMCANCEL ID", "" + alarm.id);

        // -------- START DATE --------
        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.YEAR, alarm.year);
        startCal.set(Calendar.MONTH, alarm.month);
        startCal.set(Calendar.DAY_OF_MONTH, alarm.day);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        // -------- END DATE --------
        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.YEAR, alarm.endyear);
        endCal.set(Calendar.MONTH, alarm.endmonth);
        endCal.set(Calendar.DAY_OF_MONTH, alarm.endday);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        SharedPreferences prefs =
                context.getSharedPreferences("" + alarm.id, Context.MODE_PRIVATE);

        Set<String> savedSet =
                prefs.getStringSet("KEY_REQUEST_CODES", new HashSet<>());

        Set<String> updatedSet = new HashSet<>();

        for (String item : savedSet) {

            String[] parts = item.split("::");
            int rc = Integer.parseInt(parts[0]);
            Log.e("Item", item);
            updatedSet.add(rc + "::" + "CANCELLED");
        }
        prefs.edit()
                .putStringSet("KEY_REQUEST_CODES", updatedSet)
                .apply();
        for (String day : days) {

            Calendar cal = (Calendar) startCal.clone();

            int hour24 = to24Hour(alarm.hour, alarm.amPm);
            cal.set(Calendar.HOUR_OF_DAY, hour24);
            cal.set(Calendar.MINUTE, alarm.minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            int targetDay = getDayOfWeek(day);
            int diff = targetDay - cal.get(Calendar.DAY_OF_WEEK);
            if (diff < 0) diff += 7;

            cal.add(Calendar.DAY_OF_YEAR, diff);

            while (!cal.after(endCal)) {

                int requestCode =
                        alarm.id * 100
                                + targetDay * 100
                                + cal.get(Calendar.WEEK_OF_YEAR);

                Log.e("cancel id", "" + requestCode);


                Intent intent = new Intent(context, AlarmReceiver.class);

                PendingIntent pi = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                //Log.e("AlarmCancel.....", "All scheduled alarms cancelled for: " + requestCode);
                am.cancel(pi);

                requestCode = (alarm.id + 999);
                pi = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                Log.e("cancel id_222", "" + requestCode);
                am.cancel(pi);


                cal.add(Calendar.WEEK_OF_YEAR, 1);
            }
        }

        AlarmDatabase.getInstance(context)
                .alarmDao()
                .updateAlarmTitle(alarm.id, alarm.title.replace("ON", "OFF"));


        Log.e("AlarmCancel", "All scheduled alarms cancelled for: " + alarm.title);
    }
}
