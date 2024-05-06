package com.automate.loginapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EvaluateResponse extends ResponseProcessor {

    private final Context context;

    public EvaluateResponse(Context context) {
        this.context = context;
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
        JSONArray stringResponse = response.getJSONArray("events");
        JSONObject resp = (JSONObject) stringResponse.get(0);
        if (resp != null) {
            message = resp.getString("message");
            severity = resp.getInt("severity");
        }

        Status st = new Status();
        st.setId("someId");
        st.setMessage(message);
        st.setSeverity(severity);
        return st;
    }

    private void notify(@NonNull Context context, String message, int severity) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long lastMessage = sp.getLong("lastMessage", 0);
        long elapsed = System.currentTimeMillis() - lastMessage;
        sp.edit().putLong("lastMessage", System.currentTimeMillis()).apply();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        NotificationCompat.Builder notif = new NotificationCompat.Builder(context
                , "Your_channel_id")
                .setSmallIcon(R.drawable.baseline_lock_24)
                .setContentTitle("" + message)
                .setContentText("Much longer text that cannot fit one line...")
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
            notificationManager.notify(198, notif.build());


        }
    }

}
