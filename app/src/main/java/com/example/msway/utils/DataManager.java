package com.example.msway.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.msway.models.PatientData;
import com.example.msway.models.TrainingSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Manages persistent storage for patient data, training sessions, and preferences.
 */
public class DataManager {
    private static final String TAG = "DataManager";
    private static final String PREFS_NAME = "MSWAYPrefs";
    private static final String PREF_MUSIC_GENRE = "selected_music_genre";

    private final Context context;
    private final SharedPreferences preferences;

    public DataManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ========= PATIENT DATA (JSON) =========

    public void savePatientData(PatientData patientData) {
        try {
            File dataDir = new File(context.getFilesDir(), "mSWAY_data");
            if (!dataDir.exists()) dataDir.mkdir();

            File patientsDir = new File(dataDir, "patients");
            if (!patientsDir.exists()) patientsDir.mkdir();

            File patientDataFile = new File(patientsDir, patientData.getPatientCode() + ".json");

            JSONObject json = new JSONObject();
            json.put("patientCode", patientData.getPatientCode());
            json.put("trainingDuration", patientData.getTrainingDuration());
            json.put("bestCadence", patientData.getBestCadence());
            json.put("lastModifiedBy", patientData.getLastModifiedBy());
            json.put("cadenceMode", patientData.getCadenceMode());

            // Save pattern if present
            if (patientData.getCadencePattern() != null) {
                JSONArray patternArray = new JSONArray();
                for (Long interval : patientData.getCadencePattern()) {
                    patternArray.put(interval);
                }
                json.put("cadencePattern", patternArray);
            }

            FileWriter writer = new FileWriter(patientDataFile);
            writer.write(json.toString());
            writer.flush();
            writer.close();

            Log.d(TAG, "Patient data saved as JSON: " + patientData.getPatientCode());

            saveClinicianEditLog(patientData); // 👈 Audit log

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error saving patient data", e);
        }
    }

    public PatientData getPatientData(String patientCode) {
        try {
            File patientFile = new File(context.getFilesDir(), "mSWAY_data/patients/" + patientCode + ".json");

            if (!patientFile.exists()) return null;

            BufferedReader reader = new BufferedReader(new FileReader(patientFile));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(jsonBuilder.toString());
            PatientData patientData = new PatientData();
            patientData.setPatientCode(json.getString("patientCode"));
            patientData.setTrainingDuration(json.getInt("trainingDuration"));
            patientData.setBestCadence((float) json.getDouble("bestCadence"));
            if (json.has("lastModifiedBy"))
                patientData.setLastModifiedBy(json.getString("lastModifiedBy"));
            if (json.has("cadenceMode"))
                patientData.setCadenceMode(json.getString("cadenceMode"));
            if (json.has("cadencePattern")) {
                JSONArray arr = json.getJSONArray("cadencePattern");
                List<Long> pattern = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    pattern.add(arr.getLong(i));
                }
                patientData.setCadencePattern(pattern);
            }

            return patientData;
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading patient data", e);
            return null;
        }
    }

    private void saveClinicianEditLog(PatientData data) {
        try {
            File logsDir = new File(context.getFilesDir(), "mSWAY_data/logs");
            if (!logsDir.exists()) logsDir.mkdirs();

            File logFile = new File(logsDir, data.getPatientCode() + "_log.json");

            JSONObject logEntry = new JSONObject();
            logEntry.put("timestamp", System.currentTimeMillis());
            logEntry.put("modifiedBy", data.getLastModifiedBy());
            logEntry.put("trainingDuration", data.getTrainingDuration());
            logEntry.put("bestCadence", data.getBestCadence());

            FileWriter writer = new FileWriter(logFile, true); // append
            writer.write(logEntry.toString() + "\n");
            writer.close();

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error writing clinician log", e);
        }
    }

    // ========= TRAINING SESSION DATA =========

    public void saveTrainingSession(TrainingSession session) {
        try {
            File dataDir = new File(context.getFilesDir(), "mSWAY_data");
            File subjectsDir = new File(dataDir, "subjects");
            File rawDataDir = new File(subjectsDir, "raw_data");
            File processedDataDir = new File(subjectsDir, "processed_data");

            if (!dataDir.exists()) dataDir.mkdir();
            if (!subjectsDir.exists()) subjectsDir.mkdir();
            if (!rawDataDir.exists()) rawDataDir.mkdir();
            if (!processedDataDir.exists()) processedDataDir.mkdir();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String timestamp = sdf.format(new Date());
            String filename = "session_" + timestamp + ".dat";

            File rawSessionFile = new File(rawDataDir, filename);
            FileOutputStream fosRaw = new FileOutputStream(rawSessionFile);
            ObjectOutputStream oosRaw = new ObjectOutputStream(fosRaw);
            oosRaw.writeObject(session);
            oosRaw.close();
            fosRaw.close();

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

    public List<Long> getCadencePatternForPatient(String patientCode) {
        File patternFile = new File(context.getFilesDir(), "mSWAY_data/patterns/" + patientCode + ".json");
        if (!patternFile.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(patternFile))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) json.append(line);

            JSONObject obj = new JSONObject(json.toString());
            JSONArray arr = obj.getJSONArray("intervals");

            List<Long> intervals = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                intervals.add(arr.getLong(i));
            }
            return intervals;
        } catch (IOException | JSONException e) {
            Log.e("DataManager", "Failed to read cadence pattern: " + e.getMessage());
            return null;
        }
    }

    public List<TrainingSession> getTrainingSessions() {
        List<TrainingSession> sessions = new ArrayList<>();

        try {
            File rawDataDir = new File(context.getFilesDir(), "mSWAY_data/subjects/raw_data");

            if (!rawDataDir.exists()) return sessions;

            File[] files = rawDataDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".dat")) {
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

    public void saveCadencePatternForPatient(String patientCode, List<Long> pattern) {
        try {
            File dataDir = new File(context.getFilesDir(), "mSWAY_data");
            File patternsDir = new File(dataDir, "patterns");
            if (!patternsDir.exists()) patternsDir.mkdirs();

            File patternFile = new File(patternsDir, patientCode + ".json");
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            for (Long interval : pattern) {
                arr.put(interval);
            }
            obj.put("intervals", arr);

            FileWriter writer = new FileWriter(patternFile);
            writer.write(obj.toString());
            writer.close();

            Log.d("DataManager", "Cadence pattern saved for " + patientCode);
        } catch (IOException | JSONException e) {
            Log.e("DataManager", "Failed to save pattern", e);
        }
    }


    // ========= PREFERENCES: MUSIC GENRE =========

    public void saveSelectedMusicGenre(String genre) {
        preferences.edit().putString(PREF_MUSIC_GENRE, genre).apply();
    }

    public String getSelectedMusicGenre() {
        return preferences.getString(PREF_MUSIC_GENRE, null);
    }
}
