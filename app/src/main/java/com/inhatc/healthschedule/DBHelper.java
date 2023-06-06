package com.inhatc.healthschedule;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {// 내부 DB 사용하기 위해 필요한 클래스
    private static final String DATABASE_NAME = "schedule.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE schedule (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "healthhour INTEGER," +
                "year INTEGER," +
                "month INTEGER," +
                "day INTEGER," +
                "arrived_address TEXT" +
                ")";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTableQuery = "DROP TABLE IF EXISTS schedule";
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    public void resetTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS schedule");
        onCreate(db);
        db.close();
    }

    public void clearTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("schedule", null, null);
        db.close();
    }


    public boolean deleteSchedule(int hour, int year, int month, int day, String arrived_address) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = "healthhour = ? AND year = ? AND month = ? AND day = ? AND arrived_address = ?";
        String[] whereArgs = {String.valueOf(hour), String.valueOf(year), String.valueOf(month), String.valueOf(day), arrived_address};
        int rowsAffected = db.delete("schedule", whereClause, whereArgs);
        db.close();
        return rowsAffected > 0;
    }

}
