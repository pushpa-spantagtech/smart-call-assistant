package com.mobile.sca;

import static android.content.Context.ALARM_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.sca.base.AlarmReceiver;
import com.mobile.sca.base.AlwaysOnService;

import java.util.Locale;

public class ScheduleFrag_Need extends Fragment {


    private TextView onLabel, offLabel;
    private Button setOnBtn, setOffBtn, submitBtn;
    private RecyclerView rv;
    private com.mobile.sca.base.SimpleSchedulesAdapter adapter;

    // Defaults: On 09:00, Off 18:00
    private int onHour = 9, onMinute = 0;
    private int offHour = 18, offMinute = 0;
    private boolean is24Hour = true; // set false for AM/PM

    LinearLayout daysContainer;
    Switch repeatSwitch;
    private static final int REQ_POST_NOTIF = 1001;

    @SuppressLint("ScheduleExactAlarm")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.list, container, false);
//        onLabel = root.findViewById(R.id.onTimeLabel);
//        offLabel = root.findViewById(R.id.offTimeLabel);
//        setOnBtn = root.findViewById(R.id.setOnBtn);
//        setOffBtn = root.findViewById(R.id.setOffBtn);
//        submitBtn = root.findViewById(R.id.submitBtn);
//        rv = root.findViewById(R.id.schedulesRv);
//
//        adapter = new com.mobile.sca.base.SimpleSchedulesAdapter(new ArrayList<>());
//        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
//        rv.setAdapter(adapter);
//        daysContainer = root.findViewById(R.id.daysContainer);
//        repeatSwitch = root.findViewById(R.id.repeatSwitch);
//        repeatSwitch.setOnCheckedChangeListener((btn, checked) -> {
//            daysContainer.setVisibility(checked ? View.VISIBLE : View.GONE);
//        });

//        updateLabels();

        setOnBtn.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    getActivity(),
                    (view, h, m) -> {
                        onHour = h;      // h is HOUR_OF_DAY (0–23)
                        onMinute = m;
                        updateOnLabels(onHour, onMinute);  // format to 12-hour here
                    },
                    onHour,
                    onMinute,
                    false   // 👈 IMPORTANT: false = 12-hour format
            );
            dialog.show();
        });

        setOffBtn.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    getActivity(),
                    (view, h, m) -> {
                        offHour = h;      // h is HOUR_OF_DAY (0–23)
                        offMinute = m;
                        updateffLabels(offHour, offMinute);  // format to 12-hour here
                    },
                    offHour,
                    offMinute,
                    false   // 👈 IMPORTANT: false = 12-hour format
            );
            dialog.show();
        });

        submitBtn.setOnClickListener(v -> {
            if (!isOffAfterOn(onHour, onMinute, offHour, offMinute)) {
                Toast.makeText(getActivity(), "Off time must be after On time", Toast.LENGTH_SHORT).show();
                return;
            }
            // adapter.add(new com.mobile.sca.base.SimpleSchedule(onHour, onMinute, offHour, offMinute));


            long onTimeMillis = com.mobile.sca.base.TimeUtils.nextTimeMillis(onHour, onMinute);
            long offTimeMillis = com.mobile.sca.base.TimeUtils.nextTimeMillis(offHour, offMinute);

            Log.e("OnTIme1", "" + onHour);
            Log.e("OFFTIme1", "" + onMinute);

            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
            Log.e("OnTIme", "" + onTimeMillis);
            Log.e("OFFTIme", "" + offTimeMillis);

            int count = adapter.getItemCount();
            Log.e("Count", "" + count);
// ON alarm
            Intent onIntent = new Intent(getActivity(), AlarmReceiver.class);
            onIntent.putExtra("TURN_ON", true);
            PendingIntent onPending = PendingIntent.getBroadcast(
                    getActivity(), 1001 + count, onIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, onTimeMillis, onPending);

// OFF alarm
            Intent offIntent = new Intent(getActivity(), AlarmReceiver.class);
            offIntent.putExtra("TURN_ON", false);
            PendingIntent offPending = PendingIntent.getBroadcast(
                    getActivity(), 1002 + count + 1, offIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, offTimeMillis, offPending);

            startAlwaysOnService();
            // Optional: reset to defaults after submit
            // onHour = 9; onMinute = 0; offHour = 18; offMinute = 0;
            // updateLabels();
        });

        return root;
    }

    private void updateOnLabels(int hour, int minute) {
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;

        String amPm = hour >= 12 ? "PM" : "AM";
        String time = String.format(
                Locale.getDefault(),
                "%02d:%02d %s",
                displayHour,
                minute,
                amPm
        );

        onLabel.setText("On: " + time);
    }

    private void updateffLabels(int hour, int minute) {
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;

        String amPm = hour >= 12 ? "PM" : "AM";
        String time = String.format(
                Locale.getDefault(),
                "%02d:%02d %s",
                displayHour,
                minute,
                amPm
        );

        offLabel.setText("Off: " + time);
    }

    private boolean isOffAfterOn(int oh, int om, int fh, int fm) {
        int onMin = oh * 60 + om;
        int offMin = fh * 60 + fm;
        return offMin > onMin;
    }

    private void startAlwaysOnService() {
        //Toast.makeText(getActivity(), "Start Service", Toast.LENGTH_SHORT).show();
        Intent svc = new Intent(getActivity(), AlwaysOnService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            ContextCompat.startForegroundService(getActivity(), svc);
        } else {
            getActivity().startService(svc);
        }
    }
}
