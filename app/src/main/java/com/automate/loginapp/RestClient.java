package com.automate.loginapp;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class RestClient {

    private final Context context;

    RestClient(Context context) {
        this.context = context;
    }

    public void request(int method, String domainOrIp, String port, JSONObject body,
                        ResponseProcessor er) {

        RequestQueue queue = Volley.newRequestQueue(context,
                new HurlStack(null, newSslSocketFactory()));
        String url = "https://" + domainOrIp + ":" + port;

        Response.Listener<JSONObject> listener = response -> er.process(response);
        Response.ErrorListener errorListener = volleyError -> er.processError(volleyError);

        JsonObjectRequest jsonRequest =
                new JsonObjectRequest(method, url, body, listener, errorListener) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("authorization", "Bearer " + SecurityUtils.generateToken());
                        return headers;
                    }
                };
        jsonRequest.setShouldRetryServerErrors(true);
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 3,
                1f));
        queue.add(jsonRequest);
    }

    private SSLSocketFactory    newSslSocketFactory() {
        try {
            KeyStore trusted = KeyStore.getInstance("BKS");
            InputStream in = context.getApplicationContext().getResources()
                    .openRawResource(R.raw.android);
            try {
                trusted.load(in, "PedritoClavo1Clavito".toCharArray());//SignUtil.KEYSTORE_PASSWORD
            } finally {
                in.close();
            }

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(trusted);

            String kmAlg = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(kmAlg);
            kmf.init(trusted, "PedritoClavo1Clavito".toCharArray());//SignUtil.KEYSTORE_PASSWORD

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());

            return context.getSocketFactory();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
