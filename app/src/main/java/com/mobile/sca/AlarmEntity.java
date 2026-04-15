package com.mobile.sca;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class AlarmEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int hour;
    public int minute;

    public String amPm;     // AM / PM
    public String days;     // Mon,Tue,Fri
    public String title;
    public int duration;

    // 🔥 NEW
    public int year;
    public int month;   // 0–11
    public int day;     // 1–31


    public int endyear;
    public int endmonth;   // 0–11
    public int endday;     // 1–31
}
