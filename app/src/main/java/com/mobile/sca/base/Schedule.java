package com.mobile.sca.base;
public class Schedule {
    private int id;
    private Integer onHour;
    private Integer onMinute;
    private Integer offHour;
    private Integer offMinute;

    public Schedule(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public Integer getOnHour() { return onHour; }
    public Integer getOnMinute() { return onMinute; }
    public Integer getOffHour() { return offHour; }
    public Integer getOffMinute() { return offMinute; }

    public void setOnTime(int hour, int minute) { this.onHour = hour; this.onMinute = minute; }
    public void setOffTime(int hour, int minute) { this.offHour = hour; this.offMinute = minute; }

    public String getOnTimeText() {
        if (onHour != null && onMinute != null) return String.format("%02d:%02d", onHour, onMinute);
        return "--:--";
    }

    public String getOffTimeText() {
        if (offHour != null && offMinute != null) return String.format("%02d:%02d", offHour, offMinute);
        return "--:--";
    }
}
