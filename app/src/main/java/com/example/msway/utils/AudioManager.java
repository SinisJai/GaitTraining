package com.example.msway.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.example.msway.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static final String TAG = "AudioManager";

    private Context context;
    private MediaPlayer backgroundPlayer;
    private MediaPlayer rhythmPlayer;

    // Map to store genre to audio resource mapping
    private Map<String, Integer> genreResourceMap;

    // Resource ID for the rhythm sound
    private int rhythmSoundResourceId;

    public AudioManager(Context context) {
        this.context = context;
        initializeGenreMap();

        // Create media players
        backgroundPlayer = new MediaPlayer();
        rhythmPlayer = new MediaPlayer();
    }

    private void initializeGenreMap() {
        genreResourceMap = new HashMap<>();

        // Map genre names to raw resource IDs
        // Note: These resource IDs are placeholders and should be replaced with actual resources
        genreResourceMap.put("Classical", R.raw.classical_music);
        genreResourceMap.put("Jazz", R.raw.jazz_music);
        genreResourceMap.put("Pop", R.raw.pop_music);
        genreResourceMap.put("Rock", R.raw.rock_music);
        genreResourceMap.put("Electronic", R.raw.electronic_music);
        genreResourceMap.put("Country", R.raw.country_music);
        genreResourceMap.put("Ambient", R.raw.ambient_music);
        genreResourceMap.put("Hip Hop", R.raw.hiphop_music);

        // Set rhythm sound (bass beat)
        rhythmSoundResourceId = R.raw.rhythm_sound; // Using system sound as placeholder
    }

    public void playBackgroundMusic(String genre) {
        try {
            // Reset and release any existing player
            if (backgroundPlayer.isPlaying()) {
                backgroundPlayer.stop();
            }
            backgroundPlayer.reset();

            // Get the resource ID for the selected genre
            Integer resourceId = genreResourceMap.get(genre);
            if (resourceId == null) {
                // Use default if genre not found
                resourceId = R.raw.country_music;
            }

            // Set up the media player with the resource
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resourceId);
            if (afd == null) {
                return;
            }

            backgroundPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            // Set looping and prepare
            backgroundPlayer.setLooping(true);
            backgroundPlayer.setVolume(0.7f, 0.7f); // Set volume to 70%
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

    public void playRhythmSound() {
        try {
            // Reset and release any existing rhythm player
            rhythmPlayer.reset();

            // Set up the media player with the rhythm sound resource
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(rhythmSoundResourceId);
            if (afd == null) {
                return;
            }

            rhythmPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            // Set volume and prepare
            rhythmPlayer.setVolume(1.0f, 1.0f); // Full volume for rhythm
            rhythmPlayer.prepare();
            rhythmPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "Error playing rhythm sound: " + e.getMessage());
        }
    }

    public void release() {
        if (backgroundPlayer != null) {
            if (backgroundPlayer.isPlaying()) {
                backgroundPlayer.stop();
            }
            backgroundPlayer.release();
            backgroundPlayer = null;
        }

        if (rhythmPlayer != null) {
            if (rhythmPlayer.isPlaying()) {
                rhythmPlayer.stop();
            }
            rhythmPlayer.release();
            rhythmPlayer = null;
        }
    }
}
