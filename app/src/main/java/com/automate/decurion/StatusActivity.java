package com.automate.decurion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StatusActivity extends Activity {

    public ListView statusList;
    public static ArrayAdapter<String> statusListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        LinkedList<Status> storedStatusList = SharedPreferencesHelper
                .getEntityList(getApplicationContext());
        List<String> messages = storedStatusList.stream()
                .map(item -> item.getMessage() + " - " + formatTime(item.getTime()))
                .collect(Collectors.toList());
        statusList = findViewById(R.id.statusList);
        statusListAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_selectable_list_item, android.R.id.text1, messages);
        statusList.setAdapter(statusListAdapter);

        Button buttonOpenSecondActivity = findViewById(R.id.goToMain);
        buttonOpenSecondActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StatusActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private String formatTime(long t){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(t));
        return time;
    }

}
