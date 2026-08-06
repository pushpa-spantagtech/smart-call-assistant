package com.mobile.sca;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.mobile.sca.base.AlwaysOnService;
import com.mobile.sca.base.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ScheduleFrag extends Fragment {

    public static ScheduleFrag ins;
    private TextView timeText, amText, pmText;
    private ChipGroup daysChipGroup;
    private TextInputEditText labelInput;
    private Switch vibrateSwitch;
    private Button saveAlarmBtn;

    private int hour = 9;
    private int minute = 0;
    private boolean isAm = true;

    private TextView durationText;
    private int durationMinutes = 5; // default

    private TextView dateText, endDate;
    int year, month, day;
    int endyear, endmonth, endday = 0;

    TextView b2, b1;

    LinearLayout dates, dates1, dates2, repeat;
    Calendar calendar;
    StringBuilder repeatDays;

    androidx.appcompat.widget.Toolbar bar;


    @SuppressLint("ResourceAsColor")
    public void setChipStatus() {
        for (int i = 0; i < daysChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) daysChipGroup.getChildAt(i);
            chip.setBackgroundColor(R.color.purple_200);
            chip.setChecked(true);
        }
    }

    public int getAllChipStatus() {
        int count = 0;
        for (int i = 0; i < daysChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) daysChipGroup.getChildAt(i);
            if (!chip.isChecked()) {
                count++;
            }
        }
        return count;
    }

    @SuppressLint({"WrongViewCast", "SetTextI18n"})
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.list, container, false);

        repeatDays = new StringBuilder();
        ins = ScheduleFrag.this;
        // Init views
        timeText = view.findViewById(R.id.timeText);
        amText = view.findViewById(R.id.amText);
        pmText = view.findViewById(R.id.pmText);
        daysChipGroup = view.findViewById(R.id.daysChipGroup);
        labelInput = view.findViewById(R.id.labelInput);
        vibrateSwitch = view.findViewById(R.id.vibrateSwitch);
        saveAlarmBtn = view.findViewById(R.id.saveAlarmBtn);
        durationText = view.findViewById(R.id.durationText);
        repeat = view.findViewById(R.id.repeat);
        bar = view.findViewById(R.id.bar);

        durationText.setOnClickListener(v -> showDurationPicker());

        dateText = view.findViewById(R.id.dateText);
        endDate = view.findViewById(R.id.endDateText);

        b1 = view.findViewById(R.id.b1);
        b2 = view.findViewById(R.id.b2);
        dates = view.findViewById(R.id.dates);

        dates1 = view.findViewById(R.id.dates1);
        dates2 = view.findViewById(R.id.dates2);

        Calendar now = Calendar.getInstance();

        hour = now.get(Calendar.HOUR);
        minute = now.get(Calendar.MINUTE);


        int hour24 = now.get(Calendar.HOUR_OF_DAY);

        isAm = hour24 < 12;

        updateTimeUI();
        setChipStatus();


        for (int i = 0; i < daysChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) daysChipGroup.getChildAt(i);

            chip.setOnCheckedChangeListener((button, checked) -> {
                chip.setTypeface(null, checked ? Typeface.BOLD : Typeface.NORMAL);
                if (dates.getVisibility() == VISIBLE) {
                    int count = getAllChipStatus();
                    if (count == daysChipGroup.getChildCount()) {
                        Toast.makeText(getActivity(), "Requires at least one day to schedule", Toast.LENGTH_SHORT).show();
                        chip.setChecked(true);
                    }
                }
            });
        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dates.setVisibility(VISIBLE);
                repeat.setVisibility(VISIBLE);

                for (int i = 0; i < daysChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) daysChipGroup.getChildAt(i);
                    chip.setChecked(true);
                }

                b1.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_segment_selected));
                b1.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white));
                b2.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_segment_unselected));
                b2.setTextColor(ContextCompat.getColor(requireActivity(), R.color.gray));

                saveAlarmBtn.setText(R.string.add_scheduler_button);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dates.setVisibility(GONE);
                repeat.setVisibility(GONE);

                endyear = 0;
                endmonth = 0;
                endday = 0;

                for (int i = 0; i < daysChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) daysChipGroup.getChildAt(i);
                    chip.setChecked(false);
                }


                b2.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_segment_selected));
                b2.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white));
                b1.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_segment_unselected));
                b1.setTextColor(ContextCompat.getColor(requireActivity(), R.color.gray));

                saveAlarmBtn.setText(R.string.scedule_now_button);
            }
        });

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);


        dateText.setOnClickListener(v -> showDatePicker(0));
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endyear = calendar.get(Calendar.YEAR);
                endmonth = calendar.get(Calendar.MONTH);
                endday = calendar.get(Calendar.DAY_OF_MONTH);
                showDatePicker(1);
            }
        });
        //updateTimeUI();

        // Open time picker
        timeText.setOnClickListener(v -> openTimePicker());

        // AM / PM toggle
        amText.setOnClickListener(v -> {
            isAm = true;
            updateAmPmUI();
        });

        pmText.setOnClickListener(v -> {
            isAm = false;
            updateAmPmUI();
        });

        // Save alarm
        saveAlarmBtn.setOnClickListener(v -> saveAlarm());

        SharedPreferences pref = requireActivity().getSharedPreferences("app", MODE_PRIVATE);
        String json = pref.getString("alarm", null);

        if (json != null) {
            pref.edit().putString("alarm", null).apply();
            AlarmEntity alarm =
                    new Gson().fromJson(json, AlarmEntity.class);

            Log.e("Title", alarm.title);
            bar.setTitle("Recreate schedule");
            labelInput.setText("Title");
            if (alarm.endday == 0) {

                dates.setVisibility(GONE);
                repeat.setVisibility(GONE);
                endyear = 0;
                month = 0;
                day = 0;

                b1.setEnabled(false);
                b1.setVisibility(GONE);


                for (int i = 0; i < daysChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) daysChipGroup.getChildAt(i);
                    chip.setChecked(false);
                }

                b2.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_segment_selected));
                b2.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                b1.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_segment_unselected));
                b1.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));

                saveAlarmBtn.setText("Scedule Now");
                updateDateUI(0);
                durationText.setText("" + alarm.duration + " min");

                hour = alarm.hour;
                minute = alarm.minute;
                hour24 = now.get(Calendar.HOUR_OF_DAY);
                isAm = hour24 < 12;
                updateTimeUI();

                //HomeFrag.deleteNow(alarm);
            } else {
                dates.setVisibility(VISIBLE);
                repeat.setVisibility(VISIBLE);

                b2.setEnabled(false);
                b2.setVisibility(GONE);

                b1.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_segment_selected));
                b1.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                b2.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_segment_unselected));
                b2.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));

                saveAlarmBtn.setText("Add Scheduler");

                endyear = alarm.endyear;
                endmonth = alarm.endmonth;
                endday = alarm.endday;

                year = alarm.year;
                month = alarm.month;
                day = alarm.day;

                hour = alarm.hour;
                minute = alarm.minute;
                hour24 = now.get(Calendar.HOUR_OF_DAY);
                isAm = hour24 < 12;
                updateTimeUI();

                updateDateUI(0);
                updateDateUI(1);
            }
        }

        return view;
    }

    private void openTimePicker() {

        if (getContext() == null) return;

        Calendar now = Calendar.getInstance();

        int currentHour24 = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        //int pickerHour = isAm ? hour % 12 : (hour % 12) + 12;

        TimePickerDialog dialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute1) -> {

                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selected.set(Calendar.MINUTE, minute1);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);


                    // ✅ Valid time
                    minute = minute1;
                    isAm = hourOfDay < 12;
                    hour = hourOfDay % 12;
                    if (hour == 0) hour = 12;

                    updateTimeUI();
                },
                currentHour24,
                currentMinute,
                false // 12-hour format
        );
        dialog.show();

    }

    private void updateTimeUI() {
        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        timeText.setText(time);
        updateAmPmUI();
    }

    private void updateAmPmUI() {
        amText.setTextColor(isAm ? 0xFF000000 : 0xFF888888);
        pmText.setTextColor(!isAm ? 0xFF000000 : 0xFF888888);
    }

    private void saveAlarm() {

        int count = (int) (System.currentTimeMillis() & 0x7FFFFFFF);

        Log.e("Count::;", "" + count);

        repeatDays = new StringBuilder();
        if (labelInput.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Please enter the activity name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dates.getVisibility() == VISIBLE) {
            if (dateText.getText().equals("Start Date")) {
                Toast.makeText(getActivity(), "Select start Date", Toast.LENGTH_SHORT).show();
                return;
            } else if (endDate.getText().equals("End Date")) {
                Toast.makeText(getActivity(), "Select End Date", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        for (int i = 0; i < daysChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) daysChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                repeatDays.append(chip.getText()).append(",");
            }
        }

        if (repeatDays.length() > 0) {
            repeatDays.deleteCharAt(repeatDays.length() - 1);
        }

        Log.e("Repeated Days", "." + repeatDays + ".");
        String title = labelInput.getText() != null
                ? labelInput.getText().toString()
                : "";

        AlarmEntity alarm = new AlarmEntity();
        alarm.hour = hour;
        alarm.minute = minute;
        alarm.amPm = isAm ? "AM" : "PM";
        alarm.days = repeatDays.toString();
        alarm.title = title + "::ACTIVE";
        alarm.duration = durationMinutes;

        alarm.year = year;
        alarm.month = month;
        alarm.day = day;
        alarm.id = (count + 1);

        Log.e("hour:minute", "" + hour + ":" + minute);

        if (endday != 0) {
            Log.e("Mode", "1");
            if (alarm.days.isEmpty()) {
                alarm.days = "Mon,Tue,Wed,Thu,Fri,Sat,Sun";
            }
            Calendar start = Calendar.getInstance();
            Log.e("Day", "" + day);
            Log.e("EndDay", "" + endday);

            start.set(year, month, day, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = Calendar.getInstance();
            end.set(endyear, endmonth, endday, 0, 0, 0);
            end.set(Calendar.MILLISECOND, 0);

            Calendar current = Calendar.getInstance();
            current.set(Calendar.HOUR_OF_DAY, 0);
            current.set(Calendar.MINUTE, 0);
            current.set(Calendar.SECOND, 0);
            current.set(Calendar.MILLISECOND, 0);

            String[] arr = repeatDays.toString().split(",");

            if (isOverlappingWithExisting(alarm)) {
                Toast.makeText(getActivity(),
                        "\uD83D\uDC49 “An alarm is already scheduled for this time",
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (end.getTimeInMillis() >= start.getTimeInMillis()) {
                    boolean check = isAlarmValid(year, month, day, endyear, endmonth, endday, hour, minute, isAm ? "AM" : "PM", arr);
                    if (check) {
                        if (start.before(current) || start.after(end)) {
                            Toast.makeText(getActivity(), "Invalid Date", Toast.LENGTH_SHORT).show();

                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                            String startDates = sdf.format(start.getTime());
                            String endDates = sdf.format(end.getTime());
                            Log.e("Start", startDates.toString());
                            Log.e("Start", endDates.toString());
                            dateText.setError("Invalid");
                            endDate.setError("Invalid");
                            repeatDays = new StringBuilder("".toString());
                        } else {
                            alarm.endday = endday;
                            alarm.endmonth = endmonth;
                            alarm.endyear = endyear;
                            Log.e("endDay", "" + endday);
                            Log.e("endMonth", "" + endmonth);
                            Log.e("endYear", "" + endyear);
                            // Save to DB
                            AlarmDatabase.getInstance(getContext())
                                    .alarmDao()
                                    .insertAlarm(alarm);
                            Log.e("IKJKKKKK", "" + alarm.id);
                            //cancelWeeklyAlarms(getContext(), alarm);
                            TimeUtils.scheduleWeeklyAlarmsWithDate(requireActivity(), alarm);
                            home.instance.moveTab(1);
                            startAlwaysOnService();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Invalid WeekDay schedule", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Invalid Schedule, Check manually", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (alarm.days.isEmpty()) {

            Log.e("Mode", "2");
            Calendar now = Calendar.getInstance();

            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH);        // ⚠️ 0–11
            int day = now.get(Calendar.DAY_OF_MONTH);

            alarm.year = year;
            alarm.month = month;
            alarm.day = day;
            //alarm.id = count + 1;
            boolean state = isOneTimeAlarmValid(year, month, day, hour, minute, isAm ? "AM" : "PM");

            if (isOverlappingWithExisting(alarm)) {
                Toast.makeText(getActivity(),
                        "\uD83D\uDC49 “An alarm is already scheduled for this time",
                        Toast.LENGTH_SHORT).show();
            } else {

                if (state) {
                    // Save to DB
                    AlarmDatabase.getInstance(getContext())
                            .alarmDao()
                            .insertAlarm(alarm);
                    TimeUtils.scheduleOneTimeAlarm(getContext(), alarm);
                    home.instance.moveTab(1);
                    startAlwaysOnService();

                } else {
                    Toast.makeText(getActivity(), "\uD83D\uDC49 Invalid Schedule", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean isOverlappingWithExisting(AlarmEntity newAlarm) {

        List<AlarmEntity> existingAlarms =
                AlarmDatabase.getInstance(getContext())
                        .alarmDao()
                        .getAllAlarms();

        boolean alarmconflict = false;

        for (AlarmEntity oldAlarm : existingAlarms) {
            if (oldAlarm.endday != 0) {
                SharedPreferences prefs =
                        requireActivity().getSharedPreferences("" + oldAlarm.id, Context.MODE_PRIVATE);

                Set<String> savedSet =
                        prefs.getStringSet("KEY_REQUEST_CODES", new HashSet<>());

                int finished = 0;
                for (String item : savedSet) {
                    Log.e("AdapterItem....>>>>>", item);
                    if (item.contains("ACTIVE")) {
                        break;
                    } else if (item.contains("CANCELLED")) {
                        break;
                    } else if (item.contains("Completed")) {
                        finished++;
                    } else {
                        break;
                    }
                }
                Log.e("CheckHappen", "" + savedSet.size() + ".." + finished);
                if (savedSet.size() != finished) {
                    // && !oldAlarm.title.contains("Completed")
                    boolean res = checkAlarmConflict(newAlarm, oldAlarm);
                    if (!alarmconflict) {
                        alarmconflict = res;
                    }
                }
            } else if (checkAlarmConflict(newAlarm, oldAlarm)
                    && !oldAlarm.title.contains("Completed")
            ) {
                alarmconflict = true;
            }
        }

        return alarmconflict; // ✅ safe
    }


    private boolean checkAlarmConflict(AlarmEntity newAlarm, AlarmEntity oldAlarm) {

        List<Long> newOccurrences = generateOccurrences(newAlarm);
        Log.e("newOcc", newOccurrences.toString());
        List<Long> oldOccurrences = generateOccurrences(oldAlarm);

        Log.e("OldAram", "" + oldAlarm.endday);
        Log.e("NewAlarm", "" + newAlarm.endday);

        int conflict = 0;
        for (Long newStart : newOccurrences) {
            long newEnd = newStart + (newAlarm.duration * 60 * 1000);
            for (Long oldStart : oldOccurrences) {
                long oldEnd = oldStart + (oldAlarm.duration * 60 * 1000);
                Log.e("newStart", "" + newStart);
                Log.e("oldEnd", "" + oldEnd);
                Log.e("newEnd", "" + newEnd);
                Log.e("oldStart", "" + oldStart);
                if (newStart <= oldEnd && newEnd >= oldStart) {
                    Log.e("Enter", "enter");
                    // return true; // ❌ conflict
                    conflict++;
                } else {
                    Log.e("Enter", "Failed");
                }
            }
        }
        return conflict > 0; // ✅ safe
    }

    private List<Long> generateOccurrences(AlarmEntity alarm) {

        List<Long> times = new ArrayList<>();

        Calendar start = Calendar.getInstance();
        start.set(alarm.year, alarm.month, alarm.day, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();

        if (alarm.endday != 0) {
            end.set(alarm.endyear, alarm.endmonth, alarm.endday, 0, 0, 0);
        } else {
            end.set(alarm.year, alarm.month, alarm.day, 0, 0, 0);
        }

        while (!start.after(end)) {

            if (alarm.days.isEmpty() || isDaySelected(start, alarm.days)) {

                Calendar trigger = (Calendar) start.clone();

                int hour24 = alarm.hour;
                if ("PM".equals(alarm.amPm) && hour24 < 12)
                    hour24 += 12;
                if ("AM".equals(alarm.amPm) && hour24 == 12)
                    hour24 = 0;

                trigger.set(Calendar.HOUR_OF_DAY, hour24);
                trigger.set(Calendar.MINUTE, alarm.minute);
                trigger.set(Calendar.SECOND, 0);        // ✅ FIX
                trigger.set(Calendar.MILLISECOND, 0);  // ✅ FIX

                times.add(trigger.getTimeInMillis());
            }

            start.add(Calendar.DATE, 1);
        }

        return times;
    }

    private boolean isDaySelected(Calendar dateCal, String days) {

        if (days == null || days.isEmpty())
            return false;

        String dayName = "";

        switch (dateCal.get(Calendar.DAY_OF_WEEK)) {

            case Calendar.MONDAY:
                dayName = "Mon";
                break;

            case Calendar.TUESDAY:
                dayName = "Tue";
                break;

            case Calendar.WEDNESDAY:
                dayName = "Wed";
                break;

            case Calendar.THURSDAY:
                dayName = "Thu";
                break;

            case Calendar.FRIDAY:
                dayName = "Fri";
                break;

            case Calendar.SATURDAY:
                dayName = "Sat";
                break;

            case Calendar.SUNDAY:
                dayName = "Sun";
                break;
        }

        String[] selectedDays = days.split(",");
        for (String d : selectedDays) {
            if (d.trim().equalsIgnoreCase(dayName)) {
                return true;
            }
        }
        return false;
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

    private void showDurationPicker() {

        if (getContext() == null) return;

        final String[] options = {
                "1 min",
                "2 min",
                "5 min",
                "10 min",
                "15 min",
                "30 min",
                "60 min",
                "75 min",
                "90 min"
        };

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Alarm Duration")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            durationMinutes = 1;
                            break;
                        case 1:
                            durationMinutes = 2;
                            break;
                        case 2:
                            durationMinutes = 5;
                            break;
                        case 3:
                            durationMinutes = 10;
                            break;
                        case 4:
                            durationMinutes = 15;
                            break;
                        case 5:
                            durationMinutes = 30;
                            break;
                        case 6:
                            durationMinutes = 60;
                            break;
                        case 7:
                            durationMinutes = 75;
                            break;
                        case 8:
                            durationMinutes = 90;
                            break;
                    }
                    durationText.setText(durationMinutes + " min");
                })
                .show();
    }

    private void showDatePicker(int state) {

        if (getContext() == null) return;

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (view, y, m, d) -> {
                    if (state == 0) {
                        year = y;
                        month = m;
                        day = d;
                    } else {
                        endyear = y;
                        endmonth = m;
                        endday = d;
                    }
                    updateDateUI(state);
                },
                state == 0 ? year : endyear,
                state == 0 ? month : endmonth,
                state == 0 ? day : endday
        );
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                (dialogInterface, which) -> {
                    // Cancel pressed
                    Log.d("DatePicker", "CANCELLED");
                    endyear = 0;
                    endmonth = 0;
                    endday = 0;
                    dialogInterface.dismiss();
                });
        dialog.setCancelable(false);

//        dialog.setTitle(state == 0 ? "Select From Date" : "Select To Date");
        dialog.show();
    }

    private void updateDateUI(int state) {
        String date = String.format(
                Locale.getDefault(),
                "%02d-%02d-%04d",
                state == 0 ? day : endday, state == 0 ? month + 1 : endmonth + 1, state == 0 ? year : endyear
        );
        if (state == 0) {
            dateText.setText(date);
        } else {
            endDate.setText(date);
        }
    }

    public boolean isOneTimeAlarmValid(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            String amPm
    ) {
        Calendar now = Calendar.getInstance();

        Calendar selected = Calendar.getInstance();
        selected.set(Calendar.YEAR, year);
        selected.set(Calendar.MONTH, month);
        selected.set(Calendar.DAY_OF_MONTH, day);
        selected.set(Calendar.HOUR_OF_DAY, to24Hour(hour, amPm));
        selected.set(Calendar.MINUTE, minute);
        selected.set(Calendar.SECOND, 0);
        selected.set(Calendar.MILLISECOND, 0);

        // ✅ Check SAME DATE (year, month, day must match)
        boolean sameDate =
                now.get(Calendar.YEAR) == selected.get(Calendar.YEAR) &&
                        now.get(Calendar.MONTH) == selected.get(Calendar.MONTH) &&
                        now.get(Calendar.DAY_OF_MONTH) == selected.get(Calendar.DAY_OF_MONTH);

        if (!sameDate) {
            Toast.makeText(getActivity(), "Invalid: Time is not today", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ✅ Same date → time must be in future
        boolean valid = selected.after(now);

        Log.e("AlarmCheck",
                "Now = " + now.getTime() +
                        " | Selected = " + selected.getTime() +
                        " | Valid = " + valid
        );

        return valid;
    }


    public boolean isAlarmValid(
            int year, int month, int day,
            int endYear, int endMonth, int endDay,
            int hour, int minute, String amPm,
            String[] arr
    ) {

        Calendar now = Calendar.getInstance();
        int hour24 = to24Hour(hour, amPm);

        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month, day, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(endYear, endMonth, endDay, 23, 59, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        // -------------------------------
        // 1️⃣ SAME DAY (FROM == TO)
        // -------------------------------
        if (isSameDate(startCal, endCal)) {

            Log.e("SameDate", "SameDate");
            int selectedDay = startCal.get(Calendar.DAY_OF_WEEK);

            boolean weekdayMatch = false;
            for (String d : arr) {
                Log.e("SlectedDay", "" + selectedDay + " " + d);
                Log.e("SlectedDay11", "" + getDayOfWeek(d));
                if (getDayOfWeek(d) == selectedDay) {
                    weekdayMatch = true;
                    break;
                }
            }
            Log.e("weekdayMatch", "" + weekdayMatch);

            if (!weekdayMatch) return false;

            Calendar fireCal = (Calendar) startCal.clone();
            fireCal.set(Calendar.HOUR_OF_DAY, hour24);
            fireCal.set(Calendar.MINUTE, minute);

            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

            Log.e("DATE_TIME",
                    "Fire = " + sdf.format(fireCal.getTime()) +
                            " | Now = " + sdf.format(Calendar.getInstance().getTime())
            );
            return fireCal.getTimeInMillis() > now.getTimeInMillis();
        }
        // -------------------------------
        // 2️⃣ MULTI-DAY RANGE
        // -------------------------------
        for (String d : arr) {

            int targetDay = getDayOfWeek(d);

            Calendar cal = (Calendar) startCal.clone();
            cal.set(Calendar.HOUR_OF_DAY, hour24);
            cal.set(Calendar.MINUTE, minute);

            int diff = targetDay - cal.get(Calendar.DAY_OF_WEEK);
            if (diff < 0) diff += 7;

            cal.add(Calendar.DAY_OF_YEAR, diff);

            if (cal.before(now)) {
                cal.add(Calendar.WEEK_OF_YEAR, 1);
            }

            if (!cal.after(endCal)) {
                return true; // ✅ at least one future occurrence exists
            }
        }

        return false;
    }

    static boolean isSameDate(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    static int to24Hour(int hour, String amPm) {
        if ("AM".equals(amPm)) return hour == 12 ? 0 : hour;
        return hour == 12 ? 12 : hour + 12;
    }

    int getDayOfWeek(String day) {
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

//    public void edit(AlarmEntity alarm) {
//
//        repeatDays = new StringBuilder();
//        if(labelInput.getText().toString().equals("")) {
//            Toast.makeText(getActivity(), "Please enter the activity name", Toast.LENGTH_SHORT).show();
//            return ;
//        }
//        if(dates.getVisibility() == VISIBLE) {
//            if(dateText.getText().equals("Start Date")) {
//                Toast.makeText(getActivity(), "Select start Date", Toast.LENGTH_SHORT).show();
//                return;
//            } else if(endDate.getText().equals("End Date")) {
//                Toast.makeText(getActivity(), "Select End Date", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }
//
//
//
//        for (int i = 0; i < daysChipGroup.getChildCount(); i++) {
//            Chip chip = (Chip) daysChipGroup.getChildAt(i);
//            if (chip.isChecked()) {
//                repeatDays.append(chip.getText()).append(",");
//            }
//        }
//
//        if (repeatDays.length() > 0) {
//            repeatDays.deleteCharAt(repeatDays.length() - 1);
//        }
//
//        Log.e("Repeated Days", "."+repeatDays+".");
//
//        Log.e("hour:minute", ""+hour+":"+minute);
//
//        if(endday != 0){
//            Log.e("Mode", "1");
//            if(alarm.days.isEmpty()) {
//                alarm.days = "Mon,Tue,Wed,Thu,Fri,Sat,Sun";
//            }
//            Calendar start = Calendar.getInstance();
//            Log.e("Day", ""+day);
//            Log.e("EndDay", ""+endday);
//
//            start.set(year, month, day, 0, 0, 0);
//            start.set(Calendar.MILLISECOND, 0);
//
//            Calendar end = Calendar.getInstance();
//            end.set(endyear, endmonth, endday, 0, 0, 0);
//            end.set(Calendar.MILLISECOND, 0);
//
//            Calendar current = Calendar.getInstance();
//            current.set(Calendar.HOUR_OF_DAY, 0);
//            current.set(Calendar.MINUTE, 0);
//            current.set(Calendar.SECOND, 0);
//            current.set(Calendar.MILLISECOND, 0);
//
//            String[] arr= repeatDays.toString().split(",");
//            //alarm.id = count + 1;
//
//            if (end.getTimeInMillis() >= start.getTimeInMillis()) {
//                boolean check = isAlarmValid(year, month, day, endyear, endmonth, endday, hour, minute, isAm ? "AM" : "PM", arr);
//                if (check) {
//                    if (start.before(current) || start.after(end)) {
//                        Toast.makeText(getActivity(), "Invalid Date", Toast.LENGTH_SHORT).show();
//
//                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
//
//                        String startDates = sdf.format(start.getTime());
//                        String endDates = sdf.format(end.getTime());
//                        Log.e("Start", startDates.toString());
//                        Log.e("Start", endDates.toString());
//                        dateText.setError("Invalid");
//                        endDate.setError("Invalid");
//                        repeatDays = new StringBuilder("".toString());
//                    } else {
//                        alarm.endday = endday;
//                        alarm.endmonth = endmonth;
//                        alarm.endyear = endyear;
//                        Log.e("endDay", "" + endday);
//                        Log.e("endMonth", "" + endmonth);
//                        Log.e("endYear", "" + endyear);
//                        // Save to DB
//                        AlarmDatabase.getInstance(getContext())
//                                .alarmDao()
//                                .insertAlarm(alarm);
//                        Log.e("IKJKKKKK", "" + alarm.id);
////            cancelWeeklyAlarms(getContext(), alarm);
//                        TimeUtils.scheduleWeeklyAlarmsWithDate(getContext(), alarm);
//                        home.instance.moveTab(1);
//                        startAlwaysOnService();
//                    }
//                } else {
//                    Toast.makeText(getActivity(), "Invalid WeekDay", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(getActivity(), "Invalid Schedule", Toast.LENGTH_SHORT).show();
//            }
//        }
//        else if (alarm.days.isEmpty()) {
//
//            Log.e("Mode", "2");
//            Calendar now = Calendar.getInstance();
//
//            int year  = now.get(Calendar.YEAR);
//            int month = now.get(Calendar.MONTH);        // ⚠️ 0–11
//            int day   = now.get(Calendar.DAY_OF_MONTH);
//
//            alarm.year = year;
//            alarm.month = month;
//            alarm.day = day;
//            //alarm.id = count + 1;
//
//
//
//            boolean state = isOneTimeAlarmValid(year,month,day, hour, minute, isAm ? "AM" : "PM");
//
//            if(state) {
//                // Save to DB
//                AlarmDatabase.getInstance(getContext())
//                        .alarmDao()
//                        .insertAlarm(alarm);
//                TimeUtils.scheduleOneTimeAlarm(getContext(), alarm);
//                home.instance.moveTab(1);
//                startAlwaysOnService();
//
//            } else {
//                Toast.makeText(getActivity(), "Invalid Time", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


}
