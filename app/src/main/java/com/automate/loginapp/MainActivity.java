package com.automate.loginapp;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.android.volley.Request;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    Map<Integer, ButtonHandler> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //createNotificationChannel();
        setContentView(R.layout.activity_main);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!sp.contains("lastMessage")){
            long lastMessage = System.currentTimeMillis();
            sp.edit().putLong("lastMessage", lastMessage).apply();

        }
        startPeriodicWork();




        /*Intent intent = new Intent(this, CheckWorker.class);
         *//*PendingIntent pendingIntent = PendingIntent.getBroadcast(this
                , 0, intent
                , PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pendingIntent == null){*//*
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent newIntent = PendingIntent.getBroadcast(this, 0, intent
                    , PendingIntent.FLAG_MUTABLE);
            long interval = 2 * 60 * 1000;
            long start = System.currentTimeMillis();
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, start, interval, newIntent);*/
        //}

        /*buttons = new HashMap<>();
         *//* This data would be configurable in a initial instance of the app configuration *//*
        int[] ids = {R.id.switch_1, R.id.switch_2, R.id.switch_3, R.id.switch_4};
        String[] names = {"Kitchen", "Living Room", "Garden", "Porch"};
        *//* This data would be configurable in a initial instance of the app configuration *//*

        WifiManager wifiManager = (WifiManager)
                this.getSystemService(Context.WIFI_SERVICE);
        UdpTransceiver udpTransceiver = new UdpTransceiver(wifiManager, 8286,
                8284);
        String response = udpTransceiver.sendBroadcast("SWITCH_STATUS");

        String[] statuses = response.split(":");
        *//* only when there is an error we show a message *//*
        if (statuses.length != ids.length) {
            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < statuses.length; i++) {
                Button button = findViewById(ids[i]);
                ButtonHandler bh = new ButtonHandler(ids[i], i, names[i], button,
                        statuses[i], udpTransceiver, buttons);
                buttons.put(ids[i], bh);
            }
        }*/
    }

    public void startPeriodicWork() {
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                CheckWorker.class, // Clase que extiende Worker
                5, // Intervalo mínimo de repetición
                TimeUnit.MINUTES
        ).addTag("CheckWorker")

                .build();

        //WorkManager.getInstance().cancelAllWorkByTag("CheckWorker");
        WorkManager.getInstance().enqueueUniquePeriodicWork("myUnique"
                , ExistingPeriodicWorkPolicy.REPLACE,periodicWorkRequest);
        List<WorkInfo> workInfos = WorkManager.getInstance()
                .getWorkInfosByTagLiveData("CheckWorker").getValue();
        //Toast.makeText(getApplicationContext(), workInfos.size(), Toast.LENGTH_SHORT).show();
    }

    public void onToggleClick(View v) {

        EvaluateResponse er = new EvaluateResponse(getApplicationContext());
        RestClient rc = new RestClient(getApplicationContext());
        rc.request(Request.Method.POST, "217.71.203.118", "8888/exec",
                null, er);
        //String resp = Objects.requireNonNull(buttons.get(v.getId())).toggle();
        /*NotificationCompat.Builder notif = new NotificationCompat.Builder(getApplicationContext()
                , "Your_channel_id")
                .setSmallIcon(R.drawable.baseline_lock_24)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_MAX);
        // Issue the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(198, notif.build());
            Toast.makeText(getApplicationContext(), "turn off task", Toast.LENGTH_SHORT).show();*/

        //}


    }

    private void createNotificationChannel() {
// Create the NotificationChannel, but only on API 26+ because
// the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Your_channel_id";
            String description = "Your_channel_id";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Your_channel_id", name,
                    importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviours after this
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}