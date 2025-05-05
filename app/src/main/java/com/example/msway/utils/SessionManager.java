package com.example.msway.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.msway.models.User;

import org.json.JSONException;
import org.json.JSONObject;

public class SessionManager {
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "mSWAYSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String PREF_ACTIVE_PATIENT = "active_patient";
    private static final String KEY_USER_JSON = "logged_in_user";

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setUsername(String username) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    public void setLoggedIn(boolean loggedIn) {
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setActivePatientCode(String code) {
        sharedPreferences.edit().putString(PREF_ACTIVE_PATIENT, code).apply();
    }

    public String getActivePatientCode() {
        return sharedPreferences.getString(PREF_ACTIVE_PATIENT, null);
    }

    // ✅ New method to store full User object
    public void setLoggedInUser(User user) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", user.getUsername());
            json.put("name", user.getName());
            json.put("role", user.getRole());
            sharedPreferences.edit().putString(KEY_USER_JSON, json.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ✅ New method to retrieve full User object
    public User getLoggedInUser() {
        String jsonStr = sharedPreferences.getString(KEY_USER_JSON, null);
        if (jsonStr == null) {
            String fallbackUsername = getUsername();
            if (fallbackUsername.isEmpty()) return null;
            return new User(fallbackUsername, fallbackUsername, "clinician");
        }

        try {
            JSONObject json = new JSONObject(jsonStr);
            return new User(
                    json.getString("username"),
                    json.optString("name", json.getString("username")),
                    json.optString("role", "clinician")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}