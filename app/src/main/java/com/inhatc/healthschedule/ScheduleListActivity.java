package com.inhatc.healthschedule;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;
import java.util.List;

public class ScheduleListActivity extends AppCompatActivity { //등록된 일정 보여주는 클래스
    ListView listView; // 스케줄 List
    Button btnDelete; // 삭제 버튼

    Button btnMainActivity; // MainActivity로 이동하는 버튼
    Button btnScheduleUpload; // 스케줄 등록 화면으로 이동하는 버튼

    DBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    ArrayAdapter<String> adapter;
    ArrayList<String> scheduleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        btnMainActivity = findViewById(R.id.btnMainActivity);
        btnScheduleUpload=findViewById(R.id.btnScheduleUpload);

        listView = findViewById(R.id.listView);
        btnDelete = findViewById(R.id.btnDelete);

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

            for (int j = i + 1; j < selectedPositions.size(); j++) {
                int oldPosition = selectedPositions.get(j);
                selectedPositions.set(j, oldPosition - 1);
            }

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

}
