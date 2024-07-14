package com.automate.loginapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends Activity {

    public ListView statusList;
    public static ArrayAdapter<String> statusListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        List<String> listItem = new ArrayList<>();
        statusList = findViewById(R.id.statusList);
        statusListAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_selectable_list_item, android.R.id.text1, listItem);
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

}
