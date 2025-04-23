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
        String storedHash = preferences.getString(DEFAULT_USERNAME, "");
        return !storedHash.isEmpty();
    }

    public void createDefaultAccount() {
        String passwordHash = hashPassword(DEFAULT_PASSWORD);
        if (passwordHash != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(DEFAULT_USERNAME, passwordHash);
            editor.apply();
        }
    }

    public boolean validateCredentials(String username, String password) {
        String storedHash = preferences.getString(username, "");
        if (storedHash.isEmpty()) {
            return false;
        }

        String inputHash = hashPassword(password);
        return inputHash != null && inputHash.equals(storedHash);
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
        editor.putString(username, newHash);
        editor.apply();
        return true;
    }
}
