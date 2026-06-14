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
        if (json == null) {
            return new LinkedList<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<Status>>() {
        }.getType();
        LinkedList<Status> list = gson.fromJson(json, type);
        return list != null ? list : new LinkedList<>();
    }

    public static void addItemToFront(LinkedList<Status> linkedList, Status item) {
        if (linkedList == null) {
            return;
        }
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

    private static final String HEALTH_FAIL_KEY = "healthFailCount";

    public static int getHealthFailureCount(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(HEALTH_FAIL_KEY, 0);
    }

    public static int incrementHealthFailures(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = sp.getInt(HEALTH_FAIL_KEY, 0) + 1;
        sp.edit().putInt(HEALTH_FAIL_KEY, count).apply();
        return count;
    }

    public static void resetHealthFailures(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(HEALTH_FAIL_KEY, 0).apply();
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

