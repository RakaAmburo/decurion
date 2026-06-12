package com.automate.decurion;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public abstract class ResponseProcessor {

    private boolean checkIfTimePassedAfterLastStatus;
    abstract void process(JSONObject response);
    abstract void processError(VolleyError ve);
    abstract void processError(String err);
    String basicErrorProcessing(VolleyError volleyError){
        if (volleyError == null) {
            return "VolleyError:null";
        }

        String message = "UnknownVolleyError";
        if (volleyError instanceof NoConnectionError) {
            message = "NoConnectionError";
        } else if (volleyError instanceof ServerError) {
            message = "ServerError";
        } else if (volleyError instanceof AuthFailureError) {
            message = "AuthFailureError";
        } else if (volleyError instanceof ParseError) {
            message = "ParseError";
        } else if (volleyError instanceof NetworkError) {
            message = "NetworkError";
        } else if (volleyError instanceof TimeoutError) {
            message = "TimeoutError";
        }

        if (volleyError.networkResponse != null) {
            message += "(HTTP " + volleyError.networkResponse.statusCode + ")";
        }

        if (volleyError.getMessage() != null && !volleyError.getMessage().isEmpty()) {
            message += ": " + volleyError.getMessage();
        }

        return message;
    }

    public boolean checkIfTimePassedAfterLastStatus(){
        return checkIfTimePassedAfterLastStatus;
    }

    public void setCheckIfTimePassedAfterLastStatus(boolean doCheck){
        checkIfTimePassedAfterLastStatus = doCheck;
    }
}
