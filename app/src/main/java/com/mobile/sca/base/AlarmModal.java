package com.mobile.sca.base;

import com.mobile.sca.AlarmEntity;

public class AlarmModal {

    public String time;
    public String description = "";
    public boolean enabled;

    public int duration;

    String days;

    AlarmEntity entity;

    public AlarmModal(String time, String description, boolean enabled, int duration, String days, AlarmEntity alarm) {
        this.time = time;
        this.description = description;
        this.enabled = enabled;
        this.duration = duration;
        this.days = days;
        entity = alarm;
    }
}
