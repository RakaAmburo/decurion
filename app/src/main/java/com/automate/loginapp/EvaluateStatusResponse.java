package com.automate.loginapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EvaluateStatusResponse extends ResponseProcessor {

    private final Context context;

    public EvaluateStatusResponse(Context context) {
        this.context = context;
        super.setCheckIfTimePassedAfterLastStatus(true);
    }

    @Override
    public void process(JSONObject response) {

        try {
            Status st = getText(response);
            notify(context, st.getMessage(), st.getSeverity());
        } catch (Exception e) {
            notify(context, "error parsing", 1);
        }
    }

    @Override
    public void processError(VolleyError ve) {
        String message = basicErrorProcessing(ve);
        notify(context, message, 1);
    }

    @Override
    void processError(String err) {
        notify(context, err, 1);
    }

    private Status getText(JSONObject response) throws JSONException {
        //Map resp = new HashMap();
        String message = "empty";
        int severity = 3;
        String id = "";
        JSONArray stringResponse = response.getJSONArray("events");
        JSONObject resp = (JSONObject) stringResponse.get(0);
        if (resp != null) {
            id = resp.getString("id");
            message = resp.getString("message");
            severity = resp.getInt("severity");
        }

        List<String> alist = new ArrayList<>();
        alist.add(message);
        SharedPreferencesHelper.storeEntityList(context, alist);
        List<String> returnList = SharedPreferencesHelper.getEntityList(context);
        Status st = new Status();
        st.setId(id);
        st.setMessage(returnList.get(0));
        st.setSeverity(severity);
        return st;
    }

    private void notify(@NonNull Context context, String message, int severity) {

        Intent intent = new Intent(context, StatusActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create the PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_IMMUTABLE);


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int notificationIdOld = sp.getInt("notifID", 1);
        int notificationId = (notificationIdOld == 1)?2:1;
        sp.edit().putInt("notifID", notificationId).apply();
        long lastMessage = sp.getLong("lastMessage", 0);
        long elapsed = System.currentTimeMillis() - lastMessage;
        sp.edit().putLong("lastMessage", System.currentTimeMillis()).apply();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        NotificationCompat.Builder notif = new NotificationCompat.Builder(context
                , "Your_channel_id")
                .setSmallIcon(R.drawable.baseline_lock_24)
                .setContentTitle("" + message)
                .setContentText("Much longer text that cannot fit one line...")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Last: " + minutes));
        //.setPriority(NotificationCompat.PRIORITY_MAX);
        if (severity != 1) {
            notif.setSilent(true);
            notif.setPriority(NotificationCompat.PRIORITY_MIN);
        }

        // Issue the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, notif.build());


        }
    }

}
