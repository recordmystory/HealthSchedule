package com.inhatc.healthschedule;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HealthTimeRecommend extends AppCompatActivity {
    DBHelper myDBHelper;
    SQLiteDatabase sqlDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_time_recommend);

        TextView txtHour = findViewById(R.id.txtHour); //시간
        TextView txtDate = findViewById(R.id.txtDate); //날짜

        myDBHelper = new DBHelper(this);
        sqlDB = myDBHelper.getReadableDatabase();

        // 데이터베이스에서 값을 가져와서 TextView에 설정
        String[] columns = {"healthhour", "year", "month", "day"};
        Cursor cursor = sqlDB.query("schedule", columns, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            String healthHour = cursor.getString(cursor.getColumnIndexOrThrow("healthhour"));
            int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
            int month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
            int day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));


            txtHour.setText("운동 소요시간: " + healthHour);
            txtDate.setText("날짜: " + year + "-" + (month + 1) + "-" + day);
        }

        cursor.close();
        sqlDB.close();

    }
}

       /* TextView txtHour = findViewById(R.id.txtHour); //시간
        TextView txtDate = findViewById(R.id.txtDate); //날짜

        Intent intent = getIntent();

        String hour = intent.getStringExtra("hour");
        int year = intent.getIntExtra("year", 0);
        int month = intent.getIntExtra("month", 0);
        int day = intent.getIntExtra("day", 0);

        txtHour.setText("운동시간: " + hour + "시간으로 등록되었습니다.");
        txtDate.setText("운동날짜: " + year + "년 " + (month + 1) + "월 " + day + "일" + "로 등록되었습니다."); */