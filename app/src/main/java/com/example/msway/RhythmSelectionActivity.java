package com.example.msway;

import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class RhythmSelectionActivity extends AppCompatActivity {

    private static final String TAG = "RhythmSelection";
    private static final String[] RHYTHM_NAMES = {
            "bass_pulse",
            "clap_downbeat"
    };

    private static final String PREF_NAME = "mSWAYPrefs";
    private static final String PREF_KEY = "selected_rhythm";
    private static final String DEFAULT_RHYTHM = "clap_downbeat";

    private RadioGroup rgRhythms;
    private SoundPool soundPool;
    private final HashMap<String, Integer> rhythmSoundIds = new HashMap<>();
    private final HashMap<String, Boolean> isSoundLoaded = new HashMap<>();
    private String selectedRhythm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhythm_selection);

        findViewById(R.id.btnBackRhythm).setSoundEffectsEnabled(false); //disable click sound on back button

        rgRhythms = findViewById(R.id.rgRhythms);
        findViewById(R.id.btnBackRhythm).setOnClickListener(v -> finish());

        // Load saved preference or default
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        selectedRhythm = prefs.getString(PREF_KEY, DEFAULT_RHYTHM);

        setupSoundPool();
        setupRadioButtons();
    }

    private void setupSoundPool() {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attrs)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            for (String rhythm : RHYTHM_NAMES) {
                Integer id = rhythmSoundIds.get(rhythm);
                if (id != null && id == sampleId) {
                    boolean success = (status == 0);
                    isSoundLoaded.put(rhythm, success);
                    Log.d(TAG, "Load complete for " + rhythm + ": " + (success ? "OK" : "FAIL"));
                    break;
                }
            }
        });

        // Load each sound into pool
        for (String rhythm : RHYTHM_NAMES) {
            int resId = getResources().getIdentifier(rhythm, "raw", getPackageName());
            if (resId != 0) {
                int soundId = soundPool.load(this, resId, 1);
                rhythmSoundIds.put(rhythm, soundId);
            } else {
                Log.w(TAG, "Sound resource missing: " + rhythm);
                isSoundLoaded.put(rhythm, false);
            }
        }
    }

    private void setupRadioButtons() {
        // Clear any existing buttons to prevent duplicates
        rgRhythms.removeAllViews();

        for (String rhythm : RHYTHM_NAMES) {
            RadioButton rb = new RadioButton(this);
            rb.setText(rhythm);
            rb.setId(View.generateViewId());
            rb.setSoundEffectsEnabled(false);

            if (rhythm.equals(selectedRhythm)) {
                rb.setChecked(true);
            }

            rb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedRhythm = rhythm;
                    getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                            .edit().putString(PREF_KEY, selectedRhythm).apply();
                    playSound(rhythm);
                }
            });

            rgRhythms.addView(rb,
                    new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void playSound(String rhythm) {
        Integer soundId = rhythmSoundIds.get(rhythm);
        Boolean loaded = isSoundLoaded.get(rhythm);

        if (soundId != null && Boolean.TRUE.equals(loaded)) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
        } else {
            Toast.makeText(this,
                    "Loading " + rhythm + ", please wait...",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Attempted play before load: " + rhythm);
        }
    }

    @Override
    public void onBackPressed() {
        finish(); // Just finish gracefully
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
