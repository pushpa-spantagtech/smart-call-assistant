package com.mobile.sca;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlarmListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AlarmTestAdapter adapter;
    List<AlarmEntity> alarmList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 TEMP DATA (replace with Room DB)
        getDummyAlarms();

    }

    // Dummy data for testing
    public void getDummyAlarms() {
        List<AlarmEntity> list = new ArrayList<>();
        List<AlarmEntity> alarmList =
                AlarmDatabase.getInstance(getApplicationContext())
                        .alarmDao()
                        .getAllAlarms();
        for (AlarmEntity alarm : alarmList) {
            list.add(alarm);
        }

        adapter = new AlarmTestAdapter(this, alarmList);
        recyclerView.setAdapter(adapter);
    }
}
