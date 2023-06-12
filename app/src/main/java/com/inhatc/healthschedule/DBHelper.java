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
                "arrived_address TEXT," +
                "arrived_latitude REAL," +
                "arrived_longitude REAL," +
                "exercise_check INTEGER," +
                "schedule_alarm INTEGER," +

                "exercise_gubun TEXT," +    //운동 종류
                "exercise_time INTEGER," +  //운동 소요시간
                "cost_calorie INTEGER," +   //소모 칼로리
                "total_distance INTEGER," +  //총 이동거리

                "extra TEXT" +              //완료 유무
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


    public void deleteSchedule(int hour, int year, int month, int day, String arrived_address, double arrived_latitude, double arrived_longitude) {
        SQLiteDatabase db = getWritableDatabase();
        //String whereClause = "healthhour = ? AND year = ? AND month = ? AND day = ? AND arrived_address = ?";
        //String[] whereArgs = {String.valueOf(hour), String.valueOf(year), String.valueOf(month), String.valueOf(day), arrived_address};
        //int rowsAffected = db.delete("schedule", whereClause, whereArgs);

        month--;
        db.execSQL("DELETE FROM schedule WHERE healthhour = " + hour
                + " AND year = " + year
                + " AND month = " + month
                + " AND day = " + day
                + " AND arrived_address =  '" + arrived_address + "'"
                + " AND arrived_latitude = " + arrived_latitude
                + " AND arrived_longitude = " + arrived_longitude);

        db.close();
        //return rowsAffected > 0;
    }
    public void completeScheudUpd(int hour, int year, int month, int day, String arrived_address, double arrived_latitude, double arrived_longitude, int exerciseTime, int costCalorie, int totalDistance) {
        SQLiteDatabase db = getWritableDatabase();
        //String whereClause = "healthhour = ? AND year = ? AND month = ? AND day = ? AND arrived_address = ?";
        //String[] whereArgs = {String.valueOf(hour), String.valueOf(year), String.valueOf(month), String.valueOf(day), arrived_address};
        //int rowsAffected = db.delete("schedule", whereClause, whereArgs);

        month--;
        db.execSQL("UPDATE schedule "
                + "SET exercise_time = " + exerciseTime
                + ", cost_calorie = " + costCalorie
                + ", total_distance = " + totalDistance
                + ", extra = '완료'"

                + " WHERE healthhour = " + hour
                + " AND year = " + year
                + " AND month = " + month
                + " AND day = " + day
                + " AND arrived_address =  '" + arrived_address + "'"
                + " AND arrived_latitude = " + arrived_latitude
                + " AND arrived_longitude = " + arrived_longitude);

        //UPDATE schedule SET exercise_time = 46, cost_calorie = 2, total_distance = 22, extra = 완료 WHERE healthhour = 1 AND year = 2023 AND month = 5 AND day = 12 AND arrived_address =  '대한민국 인천광역시 논현14단지등대마을' AND arrived_latitude = 37.40935209999999 AND arrived_longitude = 126.7357374

        db.close();
        //return rowsAffected > 0;
    }

}
