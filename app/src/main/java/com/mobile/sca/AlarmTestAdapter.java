package com.mobile.sca;

import static com.mobile.sca.base.TimeUtils.getDayOfWeek;
import static com.mobile.sca.base.TimeUtils.to24Hour;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.sca.base.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AlarmTestAdapter extends RecyclerView.Adapter<AlarmTestAdapter.ViewHolder> {

    Context context;
    List<AlarmEntity> list;

    public AlarmTestAdapter(Context context, List<AlarmEntity> list) {
        this.context = context;
        this.list = list;

        Log.e("Testing", ""+list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.alarm_item_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AlarmEntity alarm = list.get(position);

        holder.txtTitle.setText(alarm.title);

        holder.txtTime.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d:%02d %s",
                        alarm.hour,
                        alarm.minute,
                        alarm.amPm
                )
        );

        holder.txtDays.setText(
                alarm.days == null || alarm.days.isEmpty()
                        ? "One-time"
                        : alarm.days
        );

        String status = logAlarmStatus(alarm);
        holder.txtStatus.setText(status);

        if ("ACTIVE".equals(status)) {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_active);
        } else if ("FIRED".equals(status)) {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_fired);
        } else {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_inactive);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtTime, txtDays, txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDays = itemView.findViewById(R.id.txtDays);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }

    // 🔥 STATUS LOGIC (matches your rules)
    public static String logAlarmStatus(AlarmEntity alarm) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        Calendar now = Calendar.getInstance();

        // 1️⃣ One-time alarm (no days)
        if (alarm.days == null || alarm.days.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            cal.set(alarm.year, alarm.month, alarm.day,
                    to24Hour(alarm.hour, alarm.amPm),
                    alarm.minute, 0);
            cal.set(Calendar.MILLISECOND, 0);

            if (cal.before(now)) {
                Log.e("AlarmStatus", "One-time alarm already fired: " + alarm.title + " at " + sdf.format(cal.getTime()));
                return "FIRED";
            } else {
                Log.e("AlarmStatus", "One-time alarm scheduled: " + alarm.title + " at " + sdf.format(cal.getTime()));
                return "ACTIVE";
            }
        }

        // 2️⃣ Weekly-with-date alarm
        String[] days = alarm.days.split(",");
        Calendar startCal = Calendar.getInstance();
        startCal.set(alarm.year, alarm.month, alarm.day, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(alarm.endyear, alarm.endmonth, alarm.endday, 23, 59, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        int hour24 = to24Hour(alarm.hour, alarm.amPm);

        int scheduledCount = 0;
        int notScheduledCount = 0;
        int firedCount = 0;
        for (String day : days) {
            Calendar cal = (Calendar) startCal.clone();
            cal.set(Calendar.HOUR_OF_DAY, hour24);
            cal.set(Calendar.MINUTE, alarm.minute);

            int targetDay = getDayOfWeek(day);
            int diff = targetDay - cal.get(Calendar.DAY_OF_WEEK);
            if (diff < 0) diff += 7;
            cal.add(Calendar.DAY_OF_YEAR, diff);

            if (cal.after(endCal)) {
                Log.e("AlarmStatus", "Alarm NOT scheduled: " + alarm.title + " on " + day);
                notScheduledCount ++;
            } else if (cal.before(now)) {
                Log.e("AlarmStatus", "Alarm already fired: " + alarm.title + " on " + day + " at " + sdf.format(cal.getTime()));
                firedCount++;
            } else {
                Log.e("AlarmStatus", "Alarm scheduled: " + alarm.title + " on " + day + " at " + sdf.format(cal.getTime()));
                scheduledCount ++;
            }
        }
        return scheduledCount > 0 ? "ACTIVE" : (notScheduledCount > 0 && firedCount == 0) ? "INACTIVE" : "FIRED";
    }
}

