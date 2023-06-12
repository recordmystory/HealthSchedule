package com.inhatc.healthschedule;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ScheduleListActivity extends AppCompatActivity { //등록된 일정 보여주는 클래스
    ListView listView; // 스케줄 List
    Button btnDelete; // 삭제 버튼
    Button btnExerciseStart; // 운동 시작
    Button btnMainActivity; // MainActivity로 이동하는 버튼
    Button btnScheduleUpload; // 스케줄 등록 화면으로 이동하는 버튼

    DBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    ArrayAdapter<String> adapter;
    ArrayList<String> scheduleList;

    double arrivedLatitude = 0;
    double arrivedLongitude = 0;

    int totalCostCalorie = 0;
    int totalDistance = 0;
    int exerciseTimeSecond = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        btnMainActivity = findViewById(R.id.btnMainActivity);
        btnScheduleUpload=findViewById(R.id.btnScheduleUpload);

        listView = findViewById(R.id.listView);
        btnDelete = findViewById(R.id.btnDelete);
        btnExerciseStart = findViewById(R.id.btnExerciseStart);

        myDBHelper = new DBHelper(this);
        sqlDB = myDBHelper.getWritableDatabase();

        scheduleList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.custom_list_item, scheduleList);
        listView.setAdapter(adapter);

        showScheduleList();

        btnScheduleUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleListActivity.this, ScheduleUploadActivity.class);
                startActivity(intent);
            }
        });

        btnMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectedItems();
            }
        });

        //선택한 스케줄로 운동 시작
        btnExerciseStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NaverMap.class);
                
                List<Integer> selectedPositions = new ArrayList<>();
                SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
                for (int i = 0; i < checkedItemPositions.size(); i++) {
                    int position = checkedItemPositions.keyAt(i);
                    if (checkedItemPositions.get(position)) {
                        selectedPositions.add(position);
                    }
                }
                Log.d("Selected Positions", selectedPositions.toString());
                if(selectedPositions.isEmpty()){ // selectedPositions 리스트 비어있을 때
                    Toast.makeText(getApplicationContext(), "운동 시작할 스케줄을 선택해 주세요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (int i = selectedPositions.size() - 1; i >= 0; i--) {
                    int position = selectedPositions.get(i);
                    String schedule = scheduleList.get(position);
                    String[] scheduleParts = schedule.split("\n");
                    String[] hourParts = scheduleParts[0].split(": ");
                    int hour = Integer.parseInt(hourParts[1].replace("시간", ""));
                    String[] dateParts = scheduleParts[1].split(": ")[1].split("/");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1].replace(" ", ""));
                    int day = Integer.parseInt(dateParts[2].replace(" ", ""));
                    String arrivedAddress = scheduleParts[2].split(": ")[1];

                    //위도, 경도값 가져오기
                    String[] latitudeParts = scheduleParts[3].split(": ")[1].split(",");
                    arrivedLatitude = Double.parseDouble(latitudeParts[0]);
                    String[] longitudeParts = scheduleParts[4].split(": ")[1].split(",");
                    arrivedLongitude = Double.parseDouble(longitudeParts[0]);

                    //도착위치 NaverMap.java에 전달
                    //Intent intent = new Intent(view.getContext(), NaverMap.class);위에서 이미 선언함
                    intent.putExtra("arrivedLatitude", arrivedLatitude); //id 전달
                    intent.putExtra("arrivedLongitude", arrivedLongitude); //회원번호 전달

                    // 일정 삭제
                    //boolean isDeleted = myDBHelper.deleteSchedule(hour, year, month, day, arrivedAddress);
                    //myDBHelper.deleteSchedule(hour, year, month, day, arrivedAddress,arrivedLatitude, arrivedLongitude);
                    //Log.d("Deletion Status", "Is Deleted: " + isDeleted);
                    //Log.d("Deletion Status", "Is Deleted: ");

                    //if (isDeleted) {
                    //scheduleList.remove(position);
                    //}
                }
                //Log.d("Modified Positions", selectedPositions.toString());
                //adapter.notifyDataSetChanged();
                //clearSelection();
                
                exerciseStart.launch(intent);

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                listView.setItemChecked(position, true);
            }
        });
    }

    private void showScheduleList() {
        SQLiteDatabase db = myDBHelper.getReadableDatabase();


        Cursor cursor = db.rawQuery("SELECT * FROM schedule", null);
        scheduleList.clear();

        if (cursor.moveToFirst()) {
            do {
                int hour = cursor.getInt(cursor.getColumnIndexOrThrow("healthhour"));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                int month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
                int day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
                String arrivedAddress = cursor.getString(cursor.getColumnIndexOrThrow("arrived_address"));
                double arrivedlatitude = cursor.getDouble(cursor.getColumnIndexOrThrow("arrived_latitude"));
                double arrivedlongitude = cursor.getDouble(cursor.getColumnIndexOrThrow("arrived_longitude"));

                String schedule = "운동시간 : " + hour + "시간\n운동날짜 : " + year + "/" + (month + 1) + "/" + day + "\n도착주소 : " + arrivedAddress + "\n위도 : " + arrivedlatitude + "\n경도 : " + arrivedlongitude;
                scheduleList.add(schedule);
            } while (cursor.moveToNext());
        }

        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void deleteSelectedItems() {
        List<Integer> selectedPositions = new ArrayList<>();

        SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();

        for (int i = 0; i < checkedItemPositions.size(); i++) {
            int position = checkedItemPositions.keyAt(i);
            if (checkedItemPositions.get(position)) {
                selectedPositions.add(position);
            }
        }
        Log.d("Selected Positions", selectedPositions.toString());

        if(selectedPositions.isEmpty()){ // selectedPositions 리스트 비어있을 때
           Log.d("Deletion Status", "삭제할 항목이 선택되지 않았습니다.");
            return;
        }

        for (int i = selectedPositions.size() - 1; i >= 0; i--) {
            int position = selectedPositions.get(i);
            String schedule = scheduleList.get(position);
            String[] scheduleParts = schedule.split("\n");
            String[] hourParts = scheduleParts[0].split(": ");
            int hour = Integer.parseInt(hourParts[1].replace("시간", ""));
            String[] dateParts = scheduleParts[1].split(": ")[1].split("/");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1].replace(" ", ""));
            int day = Integer.parseInt(dateParts[2].replace(" ", ""));
            String arrivedAddress = scheduleParts[2].split(": ")[1];

            //위도, 경도값 가져오기
            String[] latitudeParts = scheduleParts[3].split(": ")[1].split(",");
            double arrivedLatitude = Double.parseDouble(latitudeParts[0]);

            String[] longitudeParts = scheduleParts[4].split(": ")[1].split(",");
            double arrivedLongitude = Double.parseDouble(longitudeParts[0]);


            // 일정 삭제
            //boolean isDeleted = myDBHelper.deleteSchedule(hour, year, month, day, arrivedAddress);
            myDBHelper.deleteSchedule(hour, year, month, day, arrivedAddress,arrivedLatitude, arrivedLongitude);
            //Log.d("Deletion Status", "Is Deleted: " + isDeleted);
            Log.d("Deletion Status", "Is Deleted: ");

            //if (isDeleted) {
            scheduleList.remove(position);
            Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();

            //}
        }
        Log.d("Modified Positions", selectedPositions.toString());

        adapter.notifyDataSetChanged();
        clearSelection();
    }

    private void clearSelection() {
        for (int i = 0; i < listView.getCount(); i++) {
            listView.setItemChecked(i, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqlDB.close();
        myDBHelper.close();
    }


    ActivityResultLauncher<Intent> exerciseStart = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult data) {
                    Log.d("TAG", "data : " + data);
                    if (data.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = data.getData();
                        //NaverMap 종료되는 시점에 소요시간&거리&열량 가져오기
                        totalDistance = intent.getIntExtra("totalDistance", 0);
                        exerciseTimeSecond = intent.getIntExtra("exerciseTimeSecond", 0);
                        totalCostCalorie = intent.getIntExtra("totalCostCalorie", 0);

                        //btnMainActivity.setText("총 이동거리 : " + totalDistance + "\n총 소요시간(초) : " + exerciseTimeSecond + "\n총 소요 칼로리 : " +  totalCostCalorie);

                        //값을 받아왔은 해당 스케줄에 Upd 후 재조회 하면 끝!

                    }
                }
            });
}
