package com.example.msway.utils;


import android.content.Context;
import android.content.SharedPreferences;
public class SessionManager {
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "mSWAYSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String PREF_ACTIVE_PATIENT = "active_patient";

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
}