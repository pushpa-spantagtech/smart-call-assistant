package com.mobile.sca;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AlarmEntity.class}, version = 1)
public abstract class AlarmDatabase extends RoomDatabase {

    private static AlarmDatabase INSTANCE;

    public abstract AlarmDao alarmDao();

    public static synchronized AlarmDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AlarmDatabase.class,
                            "alarm_db"
                    ).allowMainThreadQueries() // OK for now; can move to background later
                    .build();
        }
        return INSTANCE;
    }
}
