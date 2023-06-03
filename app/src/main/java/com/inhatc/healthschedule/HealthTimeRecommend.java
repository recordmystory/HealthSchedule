package com.inhatc.healthschedule;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HealthTimeRecommend extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_time_recommend);

        TextView txtHour = findViewById(R.id.txtHour); //시간
        TextView txtDate = findViewById(R.id.txtDate); //날짜

        Intent intent = getIntent();

        String hour = intent.getStringExtra("hour");
        int year = intent.getIntExtra("year", 0);
        int month = intent.getIntExtra("month", 0);
        int day = intent.getIntExtra("day", 0);

        txtHour.setText("운동시간: " + hour + "시간으로 등록되었습니다.");
        txtDate.setText("운동날짜: " + year + "년 " + (month + 1) + "월 " + day + "일" + "로 등록되었습니다.");
    }
}