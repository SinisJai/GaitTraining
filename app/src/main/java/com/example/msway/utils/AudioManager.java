package com.example.msway.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.msway.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static final String TAG = "AudioManager";

    private Context context;
    private MediaPlayer backgroundPlayer;

    // Map to store genre to audio resource mapping
    private final Map<String, Integer> genreResourceMap = new HashMap<>();

    public AudioManager(Context context) {
        this.context = context;
        initializeGenreMap();

        // Create media players
        backgroundPlayer = new MediaPlayer();
    }

    private void initializeGenreMap() {
        // Map genre names to raw resource IDs
        // Note: These resource IDs are placeholders and should be replaced with actual resources
        genreResourceMap.put("Italiana", R.raw.italian_music);
        genreResourceMap.put("Disco", R.raw.disco_music);
        genreResourceMap.put("Funk", R.raw.funk_music);
        genreResourceMap.put("Ambient", R.raw.ambient_music);
        genreResourceMap.put("Classical", R.raw.classical_music);

    }

    public void playBackgroundMusic(String genre) {
        try {
            // Reset and release any existing player
            if (backgroundPlayer.isPlaying()) backgroundPlayer.stop();
            backgroundPlayer.reset();

            // Get the resource ID for the selected genre
            Integer resourceId = genreResourceMap.getOrDefault(genre, R.raw.italian_music);

            // Set up the media player with the resource
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resourceId);
            if (afd == null) return;

            backgroundPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            SharedPreferences prefs = context.getSharedPreferences("mSWAYPrefs", Context.MODE_PRIVATE);
            float userVolume = prefs.getFloat("music_volume", 1.0f);
            float adjustedVolume = Math.max(0.3f, Math.min(1.0f, userVolume * 0.7f)); // Lower baseline

            // Set looping and prepare
            backgroundPlayer.setLooping(true);
            backgroundPlayer.setVolume(adjustedVolume, adjustedVolume); // Set volume to 70%
            backgroundPlayer.prepare();
            backgroundPlayer.start();

            Log.d(TAG, "Background music started: " + genre);
        } catch (IOException e) {
            Log.e(TAG, "Error playing background music: " + e.getMessage());
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundPlayer != null && backgroundPlayer.isPlaying()) {
            backgroundPlayer.stop();
            Log.d(TAG, "Background music stopped");
        }
    }

    /**
     * Dynamically plays rhythm based on selected file name and volume stored in SharedPreferences.
     */
    public void playRhythmSound() {
        try {
            SharedPreferences prefs = context.getSharedPreferences("mSWAYPrefs", Context.MODE_PRIVATE);
            String rhythmFileName = prefs.getString("selected_rhythm", "clap_downbeat");
            float rhythmVolume = prefs.getFloat("rhythm_volume", 1.0f); //full by default

            int resId = context.getResources().getIdentifier(rhythmFileName, "raw", context.getPackageName());
            if (resId == 0) {
                Log.w(TAG, "Invalid rhythm file name: " + rhythmFileName);
                return;
            }

            MediaPlayer rhythmPlayer = new MediaPlayer();
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resId);
            if (afd == null) return;

            rhythmPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            float adjustedRhythmVolume = Math.max(0.5f, Math.min(1.0f, rhythmVolume * 1.0f));// Full baseline
            rhythmPlayer.setVolume(adjustedRhythmVolume, adjustedRhythmVolume);
            rhythmPlayer.setOnCompletionListener(MediaPlayer::release);
            rhythmPlayer.prepare();
            rhythmPlayer.start();

            Log.d(TAG, "Playing rhythm: " + rhythmFileName + " at volume:" + adjustedRhythmVolume);
        } catch (IOException e) {
            Log.e(TAG, "Error playing rhythm sound: " + e.getMessage());
        }
    }

    public void release() {
        if (backgroundPlayer != null) {
            if (backgroundPlayer.isPlaying()) backgroundPlayer.stop();
            backgroundPlayer.release();
            backgroundPlayer = null;
        }
    }
}