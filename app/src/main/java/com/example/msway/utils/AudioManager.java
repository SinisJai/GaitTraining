package com.example.msway.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.msway.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static final String TAG = "AudioManager";

    private Context context;
    private ExoPlayer exoPlayer;

    // Map to store genre to audio resource mapping
    private final Map<String, Integer> genreResourceMap = new HashMap<>();

    public AudioManager(Context context) {
        this.context = context;
        initializeGenreMap();
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
     * Dynamically plays rhythm based on selected file name and volume stored in SharedPreferences.
     */
    public void playRhythmSound() {
        SharedPreferences prefs = context.getSharedPreferences("mSWAYPrefs", Context.MODE_PRIVATE);
        String rhythmFileName = prefs.getString("selected_rhythm", "clap_downbeat");
        float rhythmVolume = prefs.getFloat("rhythm_volume", 1.0f);

        int resId = context.getResources().getIdentifier(rhythmFileName, "raw", context.getPackageName());
        if (resId == 0) {
            Log.w(TAG, "Invalid rhythm file: " + rhythmFileName);
            return;
        }
        ExoPlayer rhythmPlayer = new ExoPlayer.Builder(context).build();
        MediaItem item = MediaItem.fromUri("android.resource://" + context.getPackageName() + "/" + resId);
        rhythmPlayer.setMediaItem(item);
        rhythmPlayer.setVolume(Math.max(0.5f, Math.min(1.0f, rhythmVolume)));
        rhythmPlayer.setPlaybackParameters(new PlaybackParameters(1.0f));
        rhythmPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);
        rhythmPlayer.setPlayWhenReady(true);
        rhythmPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    rhythmPlayer.release();
                }
            }
        });
        rhythmPlayer.prepare();
    }

    public void release() {
        stopBackgroundMusic();
    }
}