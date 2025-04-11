package com.automate.loginapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "CenturionPreferences";
    private static final String LIST_KEY = "statusList";

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
}

