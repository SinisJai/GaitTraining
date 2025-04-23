package com.example.msway.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.msway.models.PatientData;
import com.example.msway.models.TrainingSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataManager {
    private static final String TAG = "DataManager";
    private static final String PREFS_NAME = "MSWAYPrefs";
    private static final String PREF_MUSIC_GENRE = "selected_music_genre";

    private Context context;
    private SharedPreferences preferences;

    public DataManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void savePatientData(PatientData patientData) {
        try {
            File dataDir = new File(context.getFilesDir(), "mSWAY_data");
            if (!dataDir.exists()) {
                dataDir.mkdir();
            }

            File patientDataFile = new File(dataDir, "patient_data.dat");
            FileOutputStream fos = new FileOutputStream(patientDataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(patientData);
            oos.close();
            fos.close();

            Log.d(TAG, "Patient data saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving patient data: " + e.getMessage());
        }
    }

    public PatientData getPatientData() {
        try {
            File dataDir = new File(context.getFilesDir(), "mSWAY_data");
            File patientDataFile = new File(dataDir, "patient_data.dat");

            if (!patientDataFile.exists()) {
                return null;
            }

            FileInputStream fis = new FileInputStream(patientDataFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            PatientData patientData = (PatientData) ois.readObject();
            ois.close();
            fis.close();

            return patientData;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading patient data: " + e.getMessage());
            return null;
        }
    }

    public void saveTrainingSession(TrainingSession session) {
        try {
            File dataDir = new File(context.getFilesDir(), "mSWAY_data");
            File subjectsDir = new File(dataDir, "subjects");
            File rawDataDir = new File(subjectsDir, "raw_data");
            File processedDataDir = new File(subjectsDir, "processed_data");

            // Create directories if they don't exist
            if (!dataDir.exists()) dataDir.mkdir();
            if (!subjectsDir.exists()) subjectsDir.mkdir();
            if (!rawDataDir.exists()) rawDataDir.mkdir();
            if (!processedDataDir.exists()) processedDataDir.mkdir();

            // Generate filename with timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String timestamp = sdf.format(new Date());
            String filename = "session_" + timestamp + ".dat";

            // Save raw session data
            File rawSessionFile = new File(rawDataDir, filename);
            FileOutputStream fosRaw = new FileOutputStream(rawSessionFile);
            ObjectOutputStream oosRaw = new ObjectOutputStream(fosRaw);
            oosRaw.writeObject(session);
            oosRaw.close();
            fosRaw.close();

            // Save a summary in processed data
            File processedSessionFile = new File(processedDataDir, filename + ".summary");
            FileOutputStream fosProcessed = new FileOutputStream(processedSessionFile);
            StringBuilder summary = new StringBuilder();
            summary.append("Training Session: ").append(timestamp).append("\n");
            summary.append("Duration (mins): ").append(session.getDuration()).append("\n");
            summary.append("Target Cadence: ").append(session.getTargetCadence()).append("\n");
            summary.append("Average Cadence: ").append(session.getAverageCadence()).append("\n");
            summary.append("Music Genre: ").append(session.getMusicGenre()).append("\n");
            summary.append("Completed: ").append(session.isCompleted() ? "Yes" : "No").append("\n");

            fosProcessed.write(summary.toString().getBytes());
            fosProcessed.close();

            Log.d(TAG, "Training session saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving training session: " + e.getMessage());
        }
    }

    public List<TrainingSession> getTrainingSessions() {
        List<TrainingSession> sessions = new ArrayList<>();

        try {
            File dataDir = new File(context.getFilesDir(), "mSWAY_data");
            File subjectsDir = new File(dataDir, "subjects");
            File rawDataDir = new File(subjectsDir, "raw_data");

            if (!rawDataDir.exists()) {
                return sessions;
            }

            File[] files = rawDataDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().startsWith("session_")) {
                        FileInputStream fis = new FileInputStream(file);
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        TrainingSession session = (TrainingSession) ois.readObject();
                        sessions.add(session);
                        ois.close();
                        fis.close();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading training sessions: " + e.getMessage());
        }

        return sessions;
    }

    public void saveSelectedMusicGenre(String genre) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_MUSIC_GENRE, genre);
        editor.apply();
    }

    public String getSelectedMusicGenre() {
        return preferences.getString(PREF_MUSIC_GENRE, "");
    }
}
