package com.automate.loginapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class CheckWorker extends Worker {
    private Context context;

    public CheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }


    /*@NonNull
    @Override
    public Result doWork() {
        NotificationManager nm = (NotificationManager) ctx
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder blr = new NotificationCompat.Builder(ctx, "channel")
                .setSmallIcon(androidx.core.R.drawable.notification_icon_background)
                .setContentTitle("title")
                .setContentText("some message")
                .setPriority(1);
        nm.notify(1, blr.build());
        return null;
    }*/

    protected void notify(@NonNull Context context) {

        String homeHost = "NOT WORKED!";
        try {
            Document document = Jsoup.connect("https://raw.githubusercontent.com/RakaAmburo/nodeTasks/props/props.html").get();
            Element section = document.selectFirst("section");
            homeHost = section.selectFirst("input[name=lh]").attr("value");
        } catch (IOException e) {
            Log.v("JSOUP", "jsoup not worked");
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long lastMessage = sp.getLong("lastMessage", 0);
        long elapsed = System.currentTimeMillis() - lastMessage;
        sp.edit().putLong("lastMessage", System.currentTimeMillis()).apply();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        NotificationCompat.Builder notif = new NotificationCompat.Builder(context
                , "Your_channel_id")
                .setSmallIcon(R.drawable.baseline_lock_24)
                .setContentTitle("Ip: " + homeHost)
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Last: " + minutes))
                .setPriority(NotificationCompat.PRIORITY_MAX);
        // Issue the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(198, notif.build());
            Toast.makeText(context, "Executed", Toast.LENGTH_SHORT).show();

        }
    }


    @NonNull
    @Override
    public Result doWork() {
        //notify(getApplicationContext());
        ResponseProcessor rp = new EvaluateStatusResponse(getApplicationContext());
        RestClient rc = new RestClient(getApplicationContext());
        rc.request(Request.Method.GET, SecuredProperties.publicIp, SecuredProperties.portAndStatusPath,
                null, rp);
        return Result.success();
    }
}
