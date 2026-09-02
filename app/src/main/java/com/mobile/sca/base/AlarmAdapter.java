package com.mobile.sca.base;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.mobile.sca.AlarmDatabase;
import com.mobile.sca.AlarmEntity;
import com.mobile.sca.HomeFrag;
import com.mobile.sca.R;
import com.mobile.sca.home;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

    private static final String TAG = "AlarmAdapter";

    /*
     * AlarmReceiver uses this same offset
     * when creating the meeting END alarm.
     */
    private static final int OFF_REQUEST_OFFSET = 1_000_000;

    private Context context;
    private List<AlarmModal> list;

    RelativeLayout durlayout;

    SharedPreferences pref;

    public AlarmAdapter(Context context, List<AlarmModal> list) {
        this.context = context;
        this.list = list;

        pref = context.getSharedPreferences(
                "status",
                MODE_PRIVATE
        );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.row_alarm,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @SuppressLint({
            "DefaultLocale",
            "SetTextI18n",
            "UseCompatLoadingForDrawables"
    })
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        AlarmModal model = list.get(position);

        Log.e(
                "Test....",
                "" + model.entity.title
        );

        holder.duration.setText(
                String.format(
                        "%d minutes",
                        model.duration
                )
        );

        /*
         * IMPORTANT:
         * RecyclerView reuses ViewHolders.
         * Remove the old listener before programmatically
         * changing the switch state.
         */
        holder.switchonoff.setOnCheckedChangeListener(null);

        if (
                model.entity.day == model.entity.endday
                        && model.entity.month == model.entity.endmonth
                        && model.entity.year == model.entity.endyear
        ) {

            if (model.entity.endday != 0) {

                String date = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.day,
                        model.entity.month + 1,
                        model.entity.year
                );

                String enddate = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.endday,
                        model.entity.endmonth + 1,
                        model.entity.endyear
                );

                holder.dateRange.setText(
                        date + " - " + enddate
                );

            } else {

                holder.dateRange.setText(
                        "Single Occurrence"
                );
            }

            holder.title.setText(
                    model.entity.title
            );

        } else {

            if (model.entity.endday != 0) {

                String date = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.day,
                        model.entity.month + 1,
                        model.entity.year
                );

                String enddate = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.endday,
                        model.entity.endmonth + 1,
                        model.entity.endyear
                );

                holder.title.setText(
                        model.entity.title
                );

                holder.dateRange.setText(
                        date + " - " + enddate
                );

            } else {

                holder.title.setText(
                        model.entity.title
                );

                holder.dateRange.setText(
                        "Single Occurrence"
                );
            }
        }

        String[] parts =
                holder.title
                        .getText()
                        .toString()
                        .split("::");

        if (parts.length > 0) {
            holder.title.setText(parts[0]);
        }

        /*
         * ONE-TIME ALARM
         */
        if (model.entity.endday == 0) {

            String statusText =
                    parts.length > 1
                            ? parts[1]
                            : "ACTIVE";

            holder.status.setText(statusText);

            if (statusText.contains("ACTIVE")) {

                holder.status.setBackground(
                        context.getDrawable(
                                R.drawable.bg_active
                        )
                );

                holder.switchonoff.setChecked(true);

            } else if (statusText.contains("CANCELLED")) {

                holder.status.setBackground(
                        context.getDrawable(
                                R.drawable.bg_cancelled
                        )
                );

                holder.switchonoff.setChecked(false);

            } else if (statusText.contains("Completed")) {

                holder.status.setBackground(
                        context.getDrawable(
                                R.drawable.bg_completed
                        )
                );

                holder.switchonoff.setChecked(false);

            } else {

                /*
                 * FIRED / meeting currently active
                 */
                holder.status.setBackground(
                        context.getDrawable(
                                R.drawable.bg_status_fired
                        )
                );

                holder.switchonoff.setChecked(true);
            }

        } else {

            /*
             * DATE-RANGE / WEEKLY ALARM
             */

            SharedPreferences prefs =
                    context.getSharedPreferences(
                            String.valueOf(model.entity.id),
                            Context.MODE_PRIVATE
                    );

            Set<String> savedSet =
                    prefs.getStringSet(
                            "KEY_REQUEST_CODES",
                            new HashSet<>()
                    );

            int finished = 0;

            boolean statusFound = false;

            for (String item : savedSet) {

                Log.e(
                        "AdapterItem",
                        item
                );

                if (item.contains("ACTIVE")) {

                    holder.status.setText("ACTIVE");

                    holder.switchonoff.setChecked(true);

                    holder.status.setBackground(
                            context.getDrawable(
                                    R.drawable.bg_active
                            )
                    );

                    statusFound = true;
                    break;

                } else if (item.contains("Fired")
                        || item.contains("FIRED")) {

                    /*
                     * Meeting is currently running.
                     */
                    holder.status.setText("FIRED");

                    holder.switchonoff.setChecked(true);

                    holder.status.setBackground(
                            context.getDrawable(
                                    R.drawable.bg_status_fired
                            )
                    );

                    statusFound = true;
                    break;

                } else if (item.contains("CANCELLED")) {

                    holder.status.setText("CANCELLED");

                    holder.switchonoff.setChecked(false);

                    holder.status.setBackground(
                            context.getDrawable(
                                    R.drawable.bg_cancelled
                            )
                    );

                    statusFound = true;
                    break;

                } else if (item.contains("Completed")) {

                    finished++;
                }
            }

            if (!savedSet.isEmpty()
                    && savedSet.size() == finished) {

                holder.status.setText("Completed");

                holder.status.setBackground(
                        context.getDrawable(
                                R.drawable.bg_completed
                        )
                );

                holder.switchonoff.setChecked(false);

                statusFound = true;
            }

            /*
             * Prevent stale RecyclerView state.
             */
            if (!statusFound && savedSet.isEmpty()) {

                holder.status.setText("ACTIVE");

                holder.status.setBackground(
                        context.getDrawable(
                                R.drawable.bg_active
                        )
                );

                holder.switchonoff.setChecked(true);
            }

            Log.e(
                    "Count:;",
                    savedSet.size()
                            + " "
                            + finished
            );
        }

        holder.txtTime.setText(
                model.time
        );

        if (model.days.isEmpty()) {

            if (model.entity.endday == 0) {

                String date = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.day,
                        model.entity.month + 1,
                        model.entity.year
                );

                holder.txtDesc.setText(
                        date.replaceAll(
                                ",",
                                ", "
                        )
                );

            } else {

                holder.txtDesc.setText(
                        "Mon, Tue, Wed, Thu, Fri, Sat, Sun"
                );
            }

        } else {

            holder.txtDesc.setText(
                    model.days.replaceAll(
                            ",",
                            ", "
                    )
            );
        }

        /*
         * DELETE
         */
        holder.delete.setOnClickListener(
                view -> {

                    AlertDialog.Builder builder1 =
                            getBuilder(model);

                    builder1.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int id
                                ) {
                                    dialog.cancel();
                                }
                            }
                    );

                    AlertDialog alert11 =
                            builder1.create();

                    alert11.show();
                }
        );

        /*
         * SWITCH ON/OFF
         */
        holder.switchonoff.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(
                            @NonNull CompoundButton compoundButton,
                            boolean checked
                    ) {

                        /*
                         * USER TURNED SCHEDULE OFF
                         */
                        if (!checked) {

                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(context);

                            builder.setMessage(
                                    "Are you sure you want to turn off this schedule?"
                            );

                            builder.setTitle(
                                    "Turn Off"
                            );

                            builder.setCancelable(false);

                            builder.setPositiveButton(
                                    "Yes, Turn Off",
                                    (dialog, which) -> {

                                        /*
                                         * DATE-RANGE / WEEKLY SCHEDULE
                                         */
                                        if (model.entity.endday != 0) {

                                            /*
                                             * IMPORTANT:
                                             * If today's occurrence already fired
                                             * and DND is ON, end that active session
                                             * BEFORE cancelling future alarms.
                                             */
                                            cancelActiveRecurringDndSessions(
                                                    context,
                                                    model.entity
                                            );

                                            TimeUtils.cancelWeeklyAlarmsWithDate(
                                                    context,
                                                    model.entity
                                            );

                                            holder.switchonoff
                                                    .setOnCheckedChangeListener(null);

                                            holder.switchonoff
                                                    .setChecked(false);

                                            holder.status.setText(
                                                    "CANCELLED"
                                            );

                                            holder.status.setBackground(
                                                    context.getDrawable(
                                                            R.drawable.bg_cancelled
                                                    )
                                            );

                                        } else {

                                            /*
                                             * ONE-TIME SCHEDULE
                                             */
                                            cancelOneTimeAlarm(
                                                    context,
                                                    model.entity
                                            );

                                            holder.switchonoff
                                                    .setOnCheckedChangeListener(null);

                                            holder.switchonoff
                                                    .setChecked(false);

                                            holder.status.setText(
                                                    "CANCELLED"
                                            );

                                            holder.status.setBackground(
                                                    context.getDrawable(
                                                            R.drawable.bg_cancelled
                                                    )
                                            );
                                        }

                                        if (HomeFrag.ins != null) {
                                            HomeFrag.ins.reload();
                                        }

                                        dialog.dismiss();
                                    }
                            );

                            builder.setNegativeButton(
                                    "No",
                                    (dialog, which) -> {

                                        dialog.cancel();

                                        holder.switchonoff
                                                .setOnCheckedChangeListener(null);

                                        holder.switchonoff
                                                .setChecked(true);

                                        notifyItemChanged(
                                                holder.getBindingAdapterPosition()
                                        );
                                    }
                            );

                            AlertDialog alertDialog =
                                    builder.create();

                            alertDialog.show();

                        } else {

                            /*
                             * User is trying to re-enable an already
                             * cancelled/completed/fired schedule.
                             */
                            if (
                                    holder.status.getText().equals("OFF")
                                            || holder.status.getText().equals("FIRED")
                                            || holder.status.getText().equals("CANCELLED")
                                            || holder.status.getText().equals("Completed")
                            ) {

                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(context);

                                builder.setMessage(
                                        "Sorry, you could not enable this schedule directly, instead of turn on, you can edit and update the schedule details."
                                );

                                builder.setTitle(
                                        "Info!"
                                );

                                builder.setCancelable(false);

                                builder.setPositiveButton(
                                        "Create New",
                                        (dialog, which) -> {

                                            Gson gson =
                                                    new Gson();

                                            String alarmJson =
                                                    gson.toJson(
                                                            model.entity
                                                    );

                                            SharedPreferences appPref =
                                                    context.getSharedPreferences(
                                                            "app",
                                                            Context.MODE_PRIVATE
                                                    );

                                            appPref.edit()
                                                    .putString(
                                                            "alarm",
                                                            alarmJson
                                                    )
                                                    .apply();

                                            home.instance.moveTab(2);
                                        }
                                );

                                builder.setNegativeButton(
                                        "No",
                                        (dialog, which) ->
                                                dialog.cancel()
                                );

                                AlertDialog alertDialog =
                                        builder.create();

                                alertDialog.show();

                                holder.switchonoff
                                        .setOnCheckedChangeListener(null);

                                holder.switchonoff
                                        .setChecked(false);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtTime;
        TextView status;
        TextView txtDesc;
        TextView duration;
        TextView title;
        TextView dateRange;

        SwitchCompat switchonoff;

        RelativeLayout daysc;

        MaterialCardView card;

        Button delete;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            txtTime =
                    itemView.findViewById(
                            R.id.txtTime
                    );

            card =
                    itemView.findViewById(
                            R.id.card
                    );

            txtDesc =
                    itemView.findViewById(
                            R.id.days
                    );

            status =
                    itemView.findViewById(
                            R.id.status
                    );

            dateRange =
                    itemView.findViewById(
                            R.id.dateRange
                    );

            switchonoff =
                    itemView.findViewById(
                            R.id.switchonoff
                    );

            duration =
                    itemView.findViewById(
                            R.id.durationTxt
                    );

            title =
                    itemView.findViewById(
                            R.id.title
                    );

            delete =
                    itemView.findViewById(
                            R.id.delete
                    );
        }
    }

    private float dpToPx(float dp) {

        return dp
                * context
                .getResources()
                .getDisplayMetrics()
                .density;
    }

    /**
     * Cancel a ONE-TIME schedule.
     * <p>
     * Also ends DND immediately if the meeting has
     * already started.
     */
    public void cancelOneTimeAlarm(
            Context context,
            AlarmEntity alarm
    ) {

        int originalAlarmId =
                alarm.id;

        AlarmManager am =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );

        /*
         * Cancel meeting START alarm.
         */
        Intent startIntent =
                new Intent(
                        context,
                        AlarmReceiver.class
                );

        PendingIntent startPi =
                PendingIntent.getBroadcast(
                        context,
                        originalAlarmId,
                        startIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        if (am != null) {
            am.cancel(startPi);
        }

        startPi.cancel();

        /*
         * Update UI/database status.
         */
        String updatedTitle =
                alarm.title;

        if (updatedTitle != null) {

            updatedTitle =
                    updatedTitle
                            .replace(
                                    "ACTIVE",
                                    "CANCELLED"
                            )
                            .replace(
                                    "FIRED",
                                    "CANCELLED"
                            )
                            .replace(
                                    "Fired",
                                    "CANCELLED"
                            );
        }

        AlarmDatabase.getInstance(context)
                .alarmDao()
                .updateAlarmTitle(
                        originalAlarmId,
                        updatedTitle
                );

        /*
         * Cancel meeting END alarm.
         *
         * AlarmReceiver creates it using:
         *
         * occurrenceRequestCode + 1_000_000
         */
        int endRequestCode =
                buildEndRequestCode(
                        originalAlarmId
                );

        PendingIntent endPi =
                PendingIntent.getBroadcast(
                        context,
                        endRequestCode,
                        new Intent(
                                context,
                                AlarmReceiver.class
                        ),
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        if (am != null) {
            am.cancel(endPi);
        }

        endPi.cancel();

        /*
         * End this active DND session.
         *
         * One-time alarm occurrenceRequestCode == alarmId.
         */
        String sessionToken =
                buildSessionToken(
                        originalAlarmId,
                        originalAlarmId
                );

        DndSessionManager.endSession(
                context,
                sessionToken
        );

        /*
         * Clear old status reference if this was
         * the currently running one-time alarm.
         */
        if (
                pref.getInt(
                        "alarmId",
                        -1
                ) == originalAlarmId
        ) {

            pref.edit()
                    .remove("alarmId")
                    .remove("durationMs")
                    .apply();
        }

        Log.e(
                TAG,
                "One-time alarm cancelled: "
                        + alarm.title
        );
    }

    /**
     * Called when user manually switches OFF a
     * weekly/date-range schedule.
     * <p>
     * It finds any occurrence that has already FIRED,
     * cancels that occurrence's END alarm and removes
     * its DND session.
     */
    private void cancelActiveRecurringDndSessions(
            Context context,
            AlarmEntity alarm
    ) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        String.valueOf(alarm.id),
                        Context.MODE_PRIVATE
                );

        Set<String> savedSet =
                new HashSet<>(
                        prefs.getStringSet(
                                "KEY_REQUEST_CODES",
                                new HashSet<>()
                        )
                );

        if (savedSet.isEmpty()) {

            Log.d(
                    TAG,
                    "No recurring occurrences stored for alarm "
                            + alarm.id
            );

            return;
        }

        AlarmManager alarmManager =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );

        Set<String> updatedSet =
                new HashSet<>();

        for (String item : savedSet) {

            String[] parts =
                    item.split(
                            "::",
                            2
                    );

            if (parts.length == 0) {
                continue;
            }

            int occurrenceRequestCode;

            try {

                occurrenceRequestCode =
                        Integer.parseInt(
                                parts[0]
                        );

            } catch (NumberFormatException e) {

                Log.e(
                        TAG,
                        "Invalid recurring request code: "
                                + item,
                        e
                );

                updatedSet.add(item);
                continue;
            }

            String status =
                    parts.length > 1
                            ? parts[1]
                            : "";

            /*
             * If this occurrence has already fired,
             * then this is the currently running meeting.
             */
            if (
                    status.equalsIgnoreCase("Fired")
                            || status.equalsIgnoreCase("FIRED")
            ) {

                /*
                 * Cancel its END alarm.
                 */
                int endRequestCode =
                        buildEndRequestCode(
                                occurrenceRequestCode
                        );

                PendingIntent endPi =
                        PendingIntent.getBroadcast(
                                context,
                                endRequestCode,
                                new Intent(
                                        context,
                                        AlarmReceiver.class
                                ),
                                PendingIntent.FLAG_UPDATE_CURRENT
                                        | PendingIntent.FLAG_IMMUTABLE
                        );

                if (alarmManager != null) {
                    alarmManager.cancel(endPi);
                }

                endPi.cancel();

                /*
                 * End the exact DND session created
                 * by AlarmReceiver.
                 */
                String sessionToken =
                        buildSessionToken(
                                alarm.id,
                                occurrenceRequestCode
                        );

                DndSessionManager.endSession(
                        context,
                        sessionToken
                );

                Log.d(
                        TAG,
                        "Ended recurring DND session: "
                                + sessionToken
                );
            }

            /*
             * Once user manually switches the schedule OFF,
             * every occurrence becomes CANCELLED.
             */
            updatedSet.add(
                    occurrenceRequestCode
                            + "::CANCELLED"
            );
        }

        prefs.edit()
                .putStringSet(
                        "KEY_REQUEST_CODES",
                        updatedSet
                )
                .apply();
    }

    /**
     * Must stay identical to AlarmReceiver's
     * session token format.
     */
    private String buildSessionToken(
            int alarmId,
            int occurrenceRequestCode
    ) {

        return "alarm_"
                + alarmId
                + "_occurrence_"
                + occurrenceRequestCode;
    }

    /**
     * Must stay identical to AlarmReceiver's
     * END request-code calculation.
     */
    private int buildEndRequestCode(
            int occurrenceRequestCode
    ) {

        long candidate =
                (long) occurrenceRequestCode
                        + OFF_REQUEST_OFFSET;

        if (candidate > Integer.MAX_VALUE) {

            candidate =
                    Math.abs(
                            (long) occurrenceRequestCode
                                    * 31L
                                    + 17L
                    );
        }

        return (int) candidate;
    }

    AlertDialog.Builder getBuilder(
            AlarmModal model
    ) {

        AlertDialog.Builder builder1 =
                new AlertDialog.Builder(context);

        builder1.setMessage(
                "Are you sure you want to delete it?"
        );

        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int id
                    ) {

                        /*
                         * IMPORTANT:
                         * Cancel alarm / DND first,
                         * then delete the DB row.
                         */
                        if (model.entity.endday != 0) {

                            cancelActiveRecurringDndSessions(
                                    context,
                                    model.entity
                            );

                            TimeUtils.cancelWeeklyAlarmsWithDate(
                                    context,
                                    model.entity
                            );

                        } else {

                            cancelOneTimeAlarm(
                                    context,
                                    model.entity
                            );
                        }

                        HomeFrag.deleteNow(
                                model.entity
                        );

                        dialog.cancel();
                    }
                }
        );

        return builder1;
    }
}