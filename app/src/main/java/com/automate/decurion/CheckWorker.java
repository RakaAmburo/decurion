package com.automate.decurion;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


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
        Context ctx = getApplicationContext();
        RestClient rc = new RestClient(ctx);

        // --- Health check ---
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean healthOk = new AtomicBoolean(false);

        ResponseProcessor healthProcessor = new ResponseProcessor() {
            @Override void process(org.json.JSONObject response) { healthOk.set(true); latch.countDown(); }
            @Override void processError(com.android.volley.VolleyError ve) { latch.countDown(); }
            @Override void processError(String err) { latch.countDown(); }
        };
        healthProcessor.setCheckIfTimePassedAfterLastStatus(false);

        rc.request(Request.Method.GET, SecuredProperties.publicIp,
                SecuredProperties.portAndHealthPath, null, healthProcessor);

        try { latch.await(15, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return Result.retry(); }

        if (!healthOk.get()) {
            int failures = SharedPreferencesHelper.incrementHealthFailures(ctx);
            String msg = "HealthFailed #" + failures;
            SharedPreferencesHelper.addErrorToFront(ctx, msg);
            if (failures >= 6) {
                notifyHealthDown(ctx, failures);
            }
            return Result.failure();
        }

        // Health ok — reset counter, call /status as before (fire and forget)
        SharedPreferencesHelper.resetHealthFailures(ctx);
        ResponseProcessor rp = new EvaluateStatusResponse(ctx);
        rp.setCheckIfTimePassedAfterLastStatus(false);
        rc.request(Request.Method.GET, SecuredProperties.publicIp,
                SecuredProperties.portAndStatusPath, null, rp);
        return Result.success();
    }

    private void notifyHealthDown(Context ctx, int failureCount) {
        NotificationCompat.Builder notif = new NotificationCompat.Builder(ctx, "Your_channel_id")
                .setSmallIcon(R.drawable.baseline_lock_24)
                .setContentTitle("Servidor no responde")
                .setContentText("Health check fallido " + failureCount + " veces seguidas")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);
        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        if (ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            nm.notify(199, notif.build());
        }
    }
}
