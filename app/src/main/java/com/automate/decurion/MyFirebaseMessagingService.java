package com.automate.decurion;

import com.android.volley.Request;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.util.Log;
import android.widget.Toast;

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
        // Loguear el mensaje recibido para la depuración
        Log.d(TAG, "Mensaje recibido de: " + remoteMessage.getFrom());

        // Comprobar si el mensaje contiene una carga de datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Carga de datos del mensaje: " + remoteMessage.getData());
        }

        // Comprobar si el mensaje contiene una notificación
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Cuerpo de la notificación: " + remoteMessage.getNotification().getBody());
        }

        // Aquí puedes añadir tu lógica personalizada, como mostrar una notificación
        // en la barra de estado del teléfono.
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
}
