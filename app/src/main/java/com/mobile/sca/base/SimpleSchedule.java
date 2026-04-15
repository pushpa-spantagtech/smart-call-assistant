package com.mobile.sca.base;


public class SimpleSchedule {
    public final int onHour;
    public final int onMinute;
    public final int offHour;
    public final int offMinute;

    public SimpleSchedule(int onHour, int onMinute, int offHour, int offMinute) {
        this.onHour = onHour;
        this.onMinute = onMinute;
        this.offHour = offHour;
        this.offMinute = offMinute;
    }

    public String onText() {
        return String.format("%02d:%02d", onHour, onMinute);
    }

    public String offText() {
        return String.format("%02d:%02d", offHour, offMinute);
    }

    public String display() {
        return "On " + onText() + " \u2192 Off " + offText(); // → arrow
    }
}
