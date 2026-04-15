package com.mobile.sca;

import static android.content.Context.ALARM_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.mobile.sca.base.TimeUtils.getDayOfWeek;
import static com.mobile.sca.base.TimeUtils.to24Hour;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.sca.base.AlarmAdapter;
import com.mobile.sca.base.AlarmModal;
import com.mobile.sca.base.AlarmReceiver;
import com.mobile.sca.base.AlwaysOnService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeFrag extends Fragment {

    Button add;
    static LinearLayout empty;

    public static HomeFrag ins;

    static RecyclerView recyclerView;

    @SuppressLint("ScheduleExactAlarm")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.schdule, container, false);
        recyclerView = root.findViewById(R.id.recyclerAlarm);
        empty = root.findViewById(R.id.empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ins = HomeFrag.this;
//
        add = (Button) root.findViewById(R.id.btnAddAlarm);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                home.instance.moveTab(2);
            }
        });

        add.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(getActivity(), AlarmListActivity.class));
                return false;
            }
        });
//
        checkNow();

        return root;
    }

    public void reload() {
        checkNow();
    }
    public static void deleteNow(AlarmEntity alarm) {
        Toast.makeText(ins.getActivity(), "Successfully deleted.", Toast.LENGTH_SHORT).show();
        AlarmDatabase.getInstance(ins.getActivity())
                .alarmDao()
                .deleteAlarmById(alarm.id);

        int count = AlarmDatabase.getInstance(ins.getActivity())
                .alarmDao()
                .getAlarmCount();
        List<AlarmModal> list = new ArrayList<>();
        List<AlarmEntity> alarmList =
                AlarmDatabase.getInstance(ins.getActivity())
                        .alarmDao()
                        .getAllAlarms();
        for (AlarmEntity alarm1 : alarmList) {

            empty.setVisibility(GONE);
            int hour = alarm1.hour;
            int minute = alarm1.minute;
            String amPm = alarm1.amPm;
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            list.add(new AlarmModal(time + " " + amPm, alarm1.title, true, alarm1.duration, alarm1.days, alarm1));

        }
        AlarmAdapter adapter = new AlarmAdapter(ins.getActivity(), list);
        recyclerView.setAdapter(adapter);
        if (count == 0) {
            empty.setVisibility(VISIBLE);
        }
    }

    public void checkNow() {

        int count = AlarmDatabase.getInstance(getContext())
                .alarmDao()
                .getAlarmCount();
        if(count > 0) {

            List<AlarmModal> list = new ArrayList<>();
            List<AlarmEntity> alarmList =
                    AlarmDatabase.getInstance(getContext())
                            .alarmDao()
                            .getAllAlarms();
            for (AlarmEntity alarm : alarmList) {

                empty.setVisibility(GONE);
                int hour = alarm.hour;
                int minute = alarm.minute;
                String amPm = alarm.amPm;
                String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                list.add(new AlarmModal(time + " " + amPm, alarm.title, true, alarm.duration, alarm.days, alarm));
            }
            AlarmAdapter adapter = new AlarmAdapter(requireActivity(), list);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            empty.setVisibility(VISIBLE);
        }
    }

}
