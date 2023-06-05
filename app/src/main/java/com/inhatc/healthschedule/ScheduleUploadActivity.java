package com.inhatc.healthschedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ScheduleUploadActivity extends AppCompatActivity {

    EditText healthHour; // 운동 소요시간
    Button btnRegister; // 등록 버튼
    Button btnRegisterNaverMap; //도착위치 등록 btn
    TextView txtnull; // 운동 소요시간에 아무 값도 입력되지 않았을 때 표시될 문구


    int mYear = 0, mMonth = 0, mDay = 0;  // DatePicker 년, 월, 일

    double arrivedLatitude = 0;
    double arrivedLongitude = 0;
    String arrivedAddress = null;
    private TextView arrivedLatitudeTextView;
    private TextView arrivedLongitudeTextView;
    private TextView arrivedAddressTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_upload);

        healthHour = findViewById(R.id.healthHour);
        btnRegister = findViewById(R.id.btnRegister);
        txtnull = (TextView) findViewById(R.id.txtnull);

        arrivedLatitudeTextView = (TextView) findViewById(R.id.arrivedLatitudeTextView);
        arrivedLongitudeTextView = (TextView) findViewById(R.id.arrivedLongitudeTextView);
        arrivedAddressTextView = (TextView) findViewById(R.id.arrivedAddressTextView);





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

        //도착위치지정 btn
        btnRegisterNaverMap = findViewById(R.id.btnRegisterNaverMap);
        btnRegisterNaverMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScheduleUploadNaverMap.class);
                launcher.launch(intent);
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


    //도착위치 x,y,주소값 반환
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult data)
                {
                    Log.d("TAG", "data : " + data);
                    if (data.getResultCode() == Activity.RESULT_OK)
                    {
                        Intent intent = data.getData();
                        arrivedLatitude = intent.getDoubleExtra("arrivedLatitude", 0);
                        arrivedLongitude = intent.getDoubleExtra("arrivedLongitude", 0);
                        arrivedAddress = intent.getStringExtra("arrivedAddress");

                        arrivedLatitudeTextView.setText("위도 : " + arrivedLatitude);
                        arrivedLongitudeTextView.setText("경도 : " + arrivedLongitude);
                        arrivedAddressTextView.setText("주소 : " + arrivedAddress);
                    }
                }
            });


}