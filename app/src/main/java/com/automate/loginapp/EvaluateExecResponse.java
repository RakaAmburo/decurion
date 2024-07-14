package com.automate.loginapp;

import android.content.Context;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EvaluateExecResponse extends ResponseProcessor{

    private final Context context;

    public EvaluateExecResponse(Context context) {
        this.context = context;
    }

    @Override
    void process(JSONObject response) {
        try {
            Status st = parseJsonResponse(response);
            MainActivity.statusListAdapter.add(st.getMessage());
        } catch (Exception e) {
            MainActivity.statusListAdapter.add("error parsing");
        }
    }

    @Override
    void processError(VolleyError ve) {
        String error = basicErrorProcessing(ve);
        MainActivity.statusListAdapter.add(error);
    }

    @Override
    void processError(String err) {
        MainActivity.statusListAdapter.add(err);
    }

    private Status parseJsonResponse(JSONObject response) throws JSONException {
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
}
