package com.inhatc.healthschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ScheduleUploadActivity extends AppCompatActivity {

    EditText healthHour; // 운동 소요시간
    Button btnRegister; // 등록 버튼
    TextView txtnull; // 운동 소요시간에 아무 값도 입력되지 않았을 때 표시될 문구


    int mYear = 0, mMonth = 0, mDay = 0;  // DatePicker 년, 월, 일


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_upload);

        healthHour = findViewById(R.id.healthHour);
        btnRegister = findViewById(R.id.btnRegister);
        txtnull = (TextView) findViewById(R.id.txtnull);


        Calendar calendar = new GregorianCalendar();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePicker datePicker = findViewById(R.id.DatePicker);
        datePicker.init(mYear, mMonth, mDay, mOnDateChangedListener);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 운동 소요시간 등록 버튼 클릭시
                if (healthHour.getText().toString().length() == 0/* && healthHour.getText().equals(0)*/) { // 사용자가 아무 값도 입력하지 않은 경우
                    txtnull.setText("소요시간을 입력해주세요.");
                } else { // 사용자가 입력한 값이 있을 경우
                    Intent intent = new Intent(view.getContext(), HealthTimeRecommend.class);
                    txtnull.setText("운동시간이 등록되었습니다.");
                    intent.putExtra("hour", healthHour.getText().toString());
                    intent.putExtra("year", mYear);
                    intent.putExtra("month", mMonth);
                    intent.putExtra("day", mDay);
                    startActivity(intent);
                }
            }
        });

    }


    DatePicker.OnDateChangedListener mOnDateChangedListener = new DatePicker.OnDateChangedListener() {
        @Override
        public void onDateChanged(DatePicker view, int yy, int mm, int dd) {
            mYear = yy;
            mMonth = mm;
            mDay = dd;
        }
    };


}