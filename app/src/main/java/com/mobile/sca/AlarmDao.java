package com.mobile.sca;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlarmDao {

    @Insert
    void insertAlarm(AlarmEntity alarm);

    @Query("SELECT * FROM alarms")
    List<AlarmEntity> getAllAlarms();

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    void deleteAlarmById(int alarmId);

    @Query("SELECT COUNT(*) FROM alarms")
    int getAlarmCount();

    // ✅ UPDATE TITLE
    @Query("UPDATE alarms SET title = :newTitle WHERE id = :alarmId")
    void updateAlarmTitle(int alarmId, String newTitle);
}
