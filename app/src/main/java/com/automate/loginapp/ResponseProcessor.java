package com.automate.loginapp;

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
        String message = null;
        if (volleyError instanceof NoConnectionError) {
            message = "" + volleyError.getMessage();
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

        return message;
    }

    public boolean checkIfTimePassedAfterLastStatus(){
        return checkIfTimePassedAfterLastStatus;
    }

    public void setCheckIfTimePassedAfterLastStatus(boolean doCheck){
        checkIfTimePassedAfterLastStatus = doCheck;
    }
}
