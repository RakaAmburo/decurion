package com.automate.decurion;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "CenturionPreferences";
    private static final String LIST_KEY = "statusList";
    private static final String ERROR_KEY = "errorList";

    public static void storeEntityList(Context context, LinkedList<Status> entityList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(entityList);
        editor.putString(LIST_KEY, json);
        editor.apply();
    }

    public static LinkedList<Status> getEntityList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(LIST_KEY, null);
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<Status>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public static void addItemToFront(LinkedList<Status> linkedList, Status item) {
        linkedList.addFirst(item);
        if (linkedList.size() > 5) {
            linkedList.removeLast();
        }
    }

    public static void storeErrorList(Context context, LinkedList<Status> errorList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        editor.putString(ERROR_KEY, gson.toJson(errorList));
        editor.apply();
    }

    public static LinkedList<Status> getErrorList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(ERROR_KEY, null);
        if (json == null) return new LinkedList<>();
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<Status>>() {}.getType();
        LinkedList<Status> list = gson.fromJson(json, type);
        return list != null ? list : new LinkedList<>();
    }

    public static void addErrorToFront(Context context, String errorMessage) {
        LinkedList<Status> errors = getErrorList(context);
        Status err = new Status();
        err.setId("ERROR");
        err.setMessage(errorMessage);
        err.setSeverity(1);
        err.setTime(System.currentTimeMillis());
        errors.addFirst(err);
        if (errors.size() > 10) errors.removeLast();
        storeErrorList(context, errors);
    }
}

