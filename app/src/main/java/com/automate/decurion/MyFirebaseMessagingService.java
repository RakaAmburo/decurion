package com.automate.decurion;

import com.android.volley.Request;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // Etiqueta para el Logcat, para una fácil depuración
    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Este método es llamado cuando se recibe un mensaje de FCM.
     * Aquí puedes manejar el mensaje y mostrar una notificación.
     *
     * @param remoteMessage Objeto que contiene el mensaje de FCM
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Verifica si el mensaje contiene una notificación (título y cuerpo).
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Llama a la función para mostrar la notificación localmente
            sendNotification(title, body);
        }

        // Verifica si el mensaje contiene datos (útil para mensajes silenciosos)
        if (remoteMessage.getData().size() > 0) {
            // Aquí puedes procesar la data si es necesario, pero la notificación
            // se maneja en el bloque anterior.
        }
    }

    /**
     * Muestra la notificación en la bandeja del sistema de forma local.
     */
    private void sendNotification(String title, String body) {
        // 1. Define qué actividad abrir al hacer clic
        // Asegúrate de cambiar 'MainActivity.class' a tu actividad principal
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // 2. Crea el constructor de la notificación
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "Your_channel_id")
                        .setSmallIcon(R.drawable.baseline_warning_amber_24)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        // 3. Obtiene el gestor de notificaciones
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 4. Crea el canal de notificación (necesario para Android 8.0/Oreo o superior)
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }*/

        // 5. Muestra la notificación
        notificationManager.notify(1 /* ID de la notificación */, notificationBuilder.build());
    }

    /**
     * Este método es llamado cuando se genera un nuevo token de registro para el dispositivo.
     * Deberías enviar este token a tu servidor para poder enviar notificaciones a este dispositivo en particular.
     *
     * @param token El nuevo token de registro.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

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
            //Toast.makeText(MainActivity.this, "respuesta de broad", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.d(TAG, "Error creating JSON message!" + e.getMessage());
        }
        // Envía el nuevo token a tu servidor de aplicaciones para mantenerlo actualizado.
        // Por ejemplo: sendRegistrationToServer(token);
    }

    //pedir el token que ya existe
    /*FirebaseMessaging.getInstance().getToken()
    .addOnCompleteListener(new OnCompleteListener<String>() {
        @Override
        public void onComplete(@NonNull Task<String> task) {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();

            // Log and toast
            String msg = getString(R.string.msg_token_fmt, token);
            Log.d(TAG, msg);
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    });*/
}
