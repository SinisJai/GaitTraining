package com.example.msway.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SecurityManager {
    private static final String PREFS_NAME = "MSWAYSecurity";
    private static final String DEFAULT_USERNAME = "clinico";
    private static final String DEFAULT_PASSWORD = "mSWAY2025";

    private final SharedPreferences sharedPreferences;

    public SecurityManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Create a default admin account if none exists.
     */
    public void createDefaultAccount() {
        if (!sharedPreferences.contains("user_" + DEFAULT_USERNAME)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("user_" + DEFAULT_USERNAME, DEFAULT_PASSWORD);
            editor.apply();
        }
    }
    public boolean hasDefaultAccount() {
        return sharedPreferences.contains("user_" + DEFAULT_USERNAME);
    }

    /**
     * Validate a clinician's login credentials.
     */
    public boolean validateCredentials(String username, String password) {
        String storedPassword = sharedPreferences.getString("user_" + username, null);
        return storedPassword != null && storedPassword.equals(password);
    }

    /**
     * Change a clinician's password.
     */
    public boolean changePassword(String username, String newPassword) {
        if (!sharedPreferences.contains("user_" + username)) {
            return false; // Cannot change password if user doesn't exist
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_" + username, newPassword);
        editor.apply();
        return true;
    }

    /**
     * Create or update a new clinician account manually.
     */
    public void createOrUpdateUser(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_" + username, password);
        editor.apply();
    }

    /**
     * Check if a user exists.
     */
    public boolean userExists(String username) {
        return sharedPreferences.contains("user_" + username);
    }
}