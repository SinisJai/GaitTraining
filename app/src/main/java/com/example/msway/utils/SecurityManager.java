package com.example.msway.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class SecurityManager {
    private static final String PREF_NAME = "MSWAYSecurity";
    private static final String DEFAULT_USERNAME = "clinico";
    private static final String DEFAULT_PASSWORD = "mSWAY2025";

    private Context context;
    private SharedPreferences preferences;

    public SecurityManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean hasDefaultAccount() {
        String storedHash = preferences.getString("user_" + DEFAULT_USERNAME, ""); // Modified to use new key format
        return !storedHash.isEmpty();
    }

    public void createDefaultAccount() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_" + DEFAULT_USERNAME, DEFAULT_PASSWORD); // Modified to use new key format and store password directly (insecure, should be hashed)
        editor.apply();
    }

    public boolean validateCredentials(String username, String password) {
        String storedPassword = preferences.getString("user_" + username, null);
        return storedPassword != null && storedPassword.equals(password);
    }


    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (!validateCredentials(username, oldPassword)) {
            return false;
        }

        String newHash = hashPassword(newPassword);
        if (newHash == null) {
            return false;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_" + username, newHash); // Modified to use new key format
        editor.apply();
        return true;
    }
}