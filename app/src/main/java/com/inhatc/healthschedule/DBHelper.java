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
        // schedule 테이블 생성 쿼리
        String createTableQuery = "CREATE TABLE schedule (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "healthhour TEXT," +
                "year INTEGER," +
                "month INTEGER," +
                "day INTEGER" +
                ")";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { // 기존 테이블을 삭제하고 새로운 테이블을 생성할 때 사용

        String dropTableQuery = "DROP TABLE IF EXISTS schedule";
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    public void resetTable() { // 데이터베이스 테이블 삭제 및 재생성
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS schedule");
        onCreate(db);
        db.close();
    }

    public void clearTable() { // 데이터베이스 테이블의 모든 행 삭제
        SQLiteDatabase db = getWritableDatabase();
        db.delete("schedule", null, null);
        db.close();
    }

}
