package com.mobile.sca;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AlarmDao_Impl implements AlarmDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AlarmEntity> __insertionAdapterOfAlarmEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAlarmById;

  private final SharedSQLiteStatement __preparedStmtOfUpdateAlarmTitle;

  public AlarmDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAlarmEntity = new EntityInsertionAdapter<AlarmEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `alarms` (`id`,`hour`,`minute`,`amPm`,`days`,`title`,`duration`,`year`,`month`,`day`,`endyear`,`endmonth`,`endday`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final AlarmEntity entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.hour);
        statement.bindLong(3, entity.minute);
        if (entity.amPm == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.amPm);
        }
        if (entity.days == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.days);
        }
        if (entity.title == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.title);
        }
        statement.bindLong(7, entity.duration);
        statement.bindLong(8, entity.year);
        statement.bindLong(9, entity.month);
        statement.bindLong(10, entity.day);
        statement.bindLong(11, entity.endyear);
        statement.bindLong(12, entity.endmonth);
        statement.bindLong(13, entity.endday);
      }
    };
    this.__preparedStmtOfDeleteAlarmById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM alarms WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateAlarmTitle = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE alarms SET title = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertAlarm(final AlarmEntity alarm) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAlarmEntity.insert(alarm);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAlarmById(final int alarmId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAlarmById.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, alarmId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAlarmById.release(_stmt);
    }
  }

  @Override
  public void updateAlarmTitle(final int alarmId, final String newTitle) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateAlarmTitle.acquire();
    int _argIndex = 1;
    if (newTitle == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, newTitle);
    }
    _argIndex = 2;
    _stmt.bindLong(_argIndex, alarmId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateAlarmTitle.release(_stmt);
    }
  }

  @Override
  public List<AlarmEntity> getAllAlarms() {
    final String _sql = "SELECT * FROM alarms";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
      final int _cursorIndexOfMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
      final int _cursorIndexOfAmPm = CursorUtil.getColumnIndexOrThrow(_cursor, "amPm");
      final int _cursorIndexOfDays = CursorUtil.getColumnIndexOrThrow(_cursor, "days");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
      final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
      final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
      final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
      final int _cursorIndexOfEndyear = CursorUtil.getColumnIndexOrThrow(_cursor, "endyear");
      final int _cursorIndexOfEndmonth = CursorUtil.getColumnIndexOrThrow(_cursor, "endmonth");
      final int _cursorIndexOfEndday = CursorUtil.getColumnIndexOrThrow(_cursor, "endday");
      final List<AlarmEntity> _result = new ArrayList<AlarmEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AlarmEntity _item;
        _item = new AlarmEntity();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.hour = _cursor.getInt(_cursorIndexOfHour);
        _item.minute = _cursor.getInt(_cursorIndexOfMinute);
        if (_cursor.isNull(_cursorIndexOfAmPm)) {
          _item.amPm = null;
        } else {
          _item.amPm = _cursor.getString(_cursorIndexOfAmPm);
        }
        if (_cursor.isNull(_cursorIndexOfDays)) {
          _item.days = null;
        } else {
          _item.days = _cursor.getString(_cursorIndexOfDays);
        }
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _item.title = null;
        } else {
          _item.title = _cursor.getString(_cursorIndexOfTitle);
        }
        _item.duration = _cursor.getInt(_cursorIndexOfDuration);
        _item.year = _cursor.getInt(_cursorIndexOfYear);
        _item.month = _cursor.getInt(_cursorIndexOfMonth);
        _item.day = _cursor.getInt(_cursorIndexOfDay);
        _item.endyear = _cursor.getInt(_cursorIndexOfEndyear);
        _item.endmonth = _cursor.getInt(_cursorIndexOfEndmonth);
        _item.endday = _cursor.getInt(_cursorIndexOfEndday);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getAlarmCount() {
    final String _sql = "SELECT COUNT(*) FROM alarms";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
