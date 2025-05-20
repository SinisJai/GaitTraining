package com.example.msway.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;                                        // NEW
import android.media.SoundPool;
import android.util.Log;
import android.net.Uri;
import android.os.SystemClock;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.msway.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;                                        // NEW
import java.util.concurrent.ScheduledExecutorService;                        // NEW
import java.util.concurrent.TimeUnit;
import java.util.List;

public class AudioManager {
    private static final String TAG = "AudioManager";

    private Context context;
    private ExoPlayer exoPlayer;

    // Map to store genre to audio resource mapping
    private final Map<String, Integer> genreResourceMap = new HashMap<>();

    // NEW: SoundPool for low-latency beeps
    private SoundPool soundPool;
    private int beepSoundId;
    private ScheduledExecutorService scheduler;

    public AudioManager(Context context) {
        this.context = context;
        initializeGenreMap();

        // NEW: Initialize SoundPool and scheduler
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attrs)
                .build();
        // Load a default beep sound; replace R.raw.beep with your beep resource
        beepSoundId = soundPool.load(context, R.raw.clap_downbeat, 1);
        scheduler = Executors.newSingleThreadScheduledExecutor();
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

    public void playBackgroundMusic(String genre, float targetCadence, Runnable onPlaybackStart) {
        stopBackgroundMusic();
        SharedPreferences prefs = context.getSharedPreferences("mSWAYPrefs", Context.MODE_PRIVATE);
        prefs.edit().putFloat("target_cadence", targetCadence).apply();
        float userVolume = prefs.getFloat("music_volume", 1.0f);
        float baseBPM = 104f;
        float playbackSpeed = targetCadence / baseBPM;
        float adjustedVolume = Math.max(0.3f, Math.min(1.0f, userVolume * 0.7f));

        Integer resourceId = genreResourceMap.getOrDefault(genre, R.raw.italian_music);
        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + resourceId);

        exoPlayer = new ExoPlayer.Builder(context).build();
        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
        exoPlayer.setVolume(adjustedVolume);
        exoPlayer.setPlaybackParameters(new PlaybackParameters(playbackSpeed));
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    exoPlayer.play();
                    if (onPlaybackStart != null) onPlaybackStart.run();
                }
            }
        });
        MediaItem item = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(item);
        exoPlayer.prepare();

        Log.d(TAG, "Playing: " + genre + " at speed: " + playbackSpeed);
    }

    public void stopBackgroundMusic() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
            Log.d(TAG, "Background music stopped");
        }
    }


    /**
     +     * Schedule rhythmic beeps at the specified intervals using SoundPool.
     +     * @param rhythmPattern list of intervals (ms) between beeps.
     +     */
    public void playRhythmSound (String direction) {
        // Cancel any pending schedules
        scheduler.shutdownNow();
        scheduler = Executors.newSingleThreadScheduledExecutor();

        SharedPreferences prefs = context.getSharedPreferences("mSWAYPrefs", Context.MODE_PRIVATE);
        float rhythmVolume = prefs.getFloat("rhythm_volume", 1.0f);
        float v = Math.max(0.3f, Math.min(1.0f, rhythmVolume));

        float left = 1f, right = 1f;
        if ("L".equals(direction)) {
            left = v;
            right = 0.2f;
        } else if ("R".equals(direction)) {
            left = 0.2f;
            right = v;
        }

        soundPool.play(beepSoundId, left, right, 1, 0, 1f);
    }

    public void release() {
        stopBackgroundMusic();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
}