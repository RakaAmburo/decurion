package com.automate.decurion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivityAndRecognitionListener {

    Map<Integer, ButtonHandler> buttons;

    private TextView capturedVoiceCmd;
    private Button activateSpeechRecognitionButton;
    private Button checkStatusOnDemand;
    private Button getFireBaseToken;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    public ListView statusList;
    public static ArrayAdapter<String> statusListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //createNotificationChannel();
        setContentView(R.layout.activity_main);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!sp.contains("lastMessage")) {
            long lastMessage = System.currentTimeMillis();
            sp.edit().putLong("lastMessage", lastMessage).apply();

        }
        startPeriodicWork();

        capturedVoiceCmd = findViewById(R.id.resultMessage);
        activateSpeechRecognitionButton = findViewById(R.id.mantener);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        setTitle("Voice control 1.0");
        activateSpeechRecognitionButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    askForPermission();
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    createSpeech();
                    speech.startListening(recognizerIntent);
                    break;
                case MotionEvent.ACTION_UP:
                    capturedVoiceCmd.setText("");
                    statusListAdapter.clear();
                    break;
            }
            return false;
        });

        checkStatusOnDemand = findViewById(R.id.status);
        checkStatusOnDemand.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ResponseProcessor rp = new EvaluateStatusResponse(getApplicationContext());
                    rp.setCheckIfTimePassedAfterLastStatus(false);
                    RestClient rc = new RestClient(getApplicationContext());
                    rc.request(Request.Method.GET, SecuredProperties.publicIp, SecuredProperties.portAndStatusPath,
                            null, rp);
                    Intent intent = new Intent(MainActivity.this, StatusActivity.class);
                    startActivity(intent);
                    break;
                case MotionEvent.ACTION_UP:
                    /*capturedVoiceCmd.setText("");
                    statusListAdapter.clear();*/
                    break;
            }
            return false;
        });

        getFireBaseToken = findViewById(R.id.getToken);
        getFireBaseToken.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {

                                if (!task.isSuccessful()) {
                                    Log.w("GetFBToken", "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Get new FCM registration token
                                String token = task.getResult();

                                JSONObject jsonObject = new JSONObject();
                                JSONArray matchesArray = new JSONArray();
                                JSONObject extrasObject = new JSONObject();
                                try {
                                    matchesArray.put("store new token");
                                    jsonObject.put("possibleMessages", matchesArray);
                                    extrasObject.put("token", token);
                                    jsonObject.put("extras", extrasObject);
                                    ResponseProcessor rp = new EvaluateExecResponse(getApplicationContext());
                                    RestClient rc = new RestClient(getApplicationContext());
                                    rc.request(Request.Method.POST, SecuredProperties.publicIp, SecuredProperties.portAndExecPath,
                                            jsonObject, rp);
                                    Toast.makeText(MainActivity.this, "Token enviado", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    Log.d("GetFBToken", "Error creating JSON message!" + e.getMessage());
                                }
                            });
                    break;
                case MotionEvent.ACTION_UP:
                    /*capturedVoiceCmd.setText("");
                    statusListAdapter.clear();*/
                    break;
            }
            return false;
        });

        List<String> listItem = new ArrayList<>();
        statusList = findViewById(R.id.statusList);
        statusListAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_selectable_list_item, android.R.id.text1, listItem);
        statusList.setAdapter(statusListAdapter);
        //statusList.setOnItemClickListener(new ClickListener(this));

        Button buttonOpenSecondActivity = findViewById(R.id.goToStatus);
        buttonOpenSecondActivity.setOnTouchListener((v, event) -> {
            Intent intent = new Intent(MainActivity.this, StatusActivity.class);
            startActivity(intent);
            return false;
        });

    }

    private void createSpeech() {
        if (speech != null) {
            speech.destroy();
        }
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    private void askForPermission() {
        String permission = "android.permission.RECORD_AUDIO";
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    permission)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission}, 1);
            }
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        progressBar.setIndeterminate(true);
        //worked
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
        //speech.stopListening();
    }

    @Override
    public void onError(int errorCode) {
        if (errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
            createSpeech();
            speech.startListening(recognizerIntent);
            return;
        }
        if (speech != null) {
            speech.destroy();
        }
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
        String errorMessage = getErrorText(errorCode);
        capturedVoiceCmd.setText(errorMessage);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client retry";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    private long lastClickTime = 0;

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (SystemClock.elapsedRealtime() - lastClickTime < 2000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        if (matches.size() == 0) {
            capturedVoiceCmd.setText("Listening Service Error");
        } else {
            capturedVoiceCmd.setText(matches.get(0));
            //if (matches.get(0).equalsIgnoreCase("computer unlock")){
            //String message = sendBroadcast("UNLOCK_MACHINE");
            JSONObject jsonObject = new JSONObject();
            JSONArray matchesArray = new JSONArray();
            try {
                for (String result : matches) {
                    matchesArray.put(result);
                }
                jsonObject.put("possibleMessages", matchesArray);
                ResponseProcessor rp = new EvaluateExecResponse(getApplicationContext());
                RestClient rc = new RestClient(getApplicationContext());
                rc.request(Request.Method.POST, SecuredProperties.publicIp, SecuredProperties.portAndExecPath,
                        jsonObject, rp);
                //Toast.makeText(MainActivity.this, "respuesta de broad", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "Error creating JSON message!",
                        Toast.LENGTH_SHORT).show();
            }

            //}
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (speech == null) {
            createSpeech();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
        }
    }

    public void startPeriodicWork() {

        List<WorkInfo> workInfos = WorkManager.getInstance(this)
                .getWorkInfosByTagLiveData("myUnique").getValue();
        if (workInfos == null) {
            //Toast.makeText(getApplicationContext(), "instanteating works", Toast.LENGTH_SHORT).show();
            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                    CheckWorker.class, // Clase que extiende Worker
                    5, // Intervalo mínimo de repetición
                    TimeUnit.MINUTES
            ).addTag("CheckWorker")
                    .build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork("myUnique"
                    , ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
        }

    }

    public void onToggleClick(View v) {

        EvaluateStatusResponse er = new EvaluateStatusResponse(getApplicationContext());
        RestClient rc = new RestClient(getApplicationContext());
        rc.request(Request.Method.GET, SecuredProperties.publicIp, SecuredProperties.portAndStatusPath,
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