package com.example.msway;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;


import com.example.msway.models.PatientData;
import com.example.msway.models.TrainingSession;
import com.example.msway.utils.AudioManager;
import com.example.msway.utils.DataManager;
import com.example.msway.utils.SensorManager;
import com.example.msway.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// Activity principale per la fase di allenamento del paziente
public class TrainingActivity extends AppCompatActivity {
    // Componenti UI
    private TextView tvTimer;
    private TextView tvCurrentCadence;
    private TextView tvTargetCadence;
    private TextView tvMusicGenre;
    private ProgressBar pbTrainingProgress;
    private Button btnStopTraining;

    // Gestione dati, sessione, audio e sensori
    private DataManager dataManager;
    private SessionManager sessionManager;
    private AudioManager audioManager;
    private SensorManager sensorManager;
    private Vibrator vibrator;

    // Dati del paziente e parametri di allenamento
    private PatientData patientData;
    private String selectedMusicGenre;

    // Timer e variabili per gestione della sessione
    private CountDownTimer trainingTimer;
    private boolean isTrainingActive = false;
    private long trainingDurationMs;
    private long remainingTimeMs;
    private float targetCadence;
    private float currentCadence;

    // Gestione del ritmo sonoro
    private Handler rhythmHandler = new Handler();
    private Runnable rhythmRunnable;
    private long rhythmInterval;
    private List<Long> rhythmPattern;
    private int intervalIndex = 0;


    private TrainingSession currentSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);// Layout edge-to-edge
        setContentView(R.layout.activity_training);
        // Adatta layout alle barre di sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainTrainingLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeComponents();      // Inizializza componenti UI e manager
        prepareTrainingSession();    // Prepara la sessione d’allenamento
    }

    // Inizializza gli elementi grafici e la logica dei pulsanti
    private void initializeComponents() {
        tvTimer = findViewById(R.id.tvTimer);
        tvCurrentCadence = findViewById(R.id.tvCurrentCadence);
        tvTargetCadence = findViewById(R.id.tvTargetCadence);
        tvMusicGenre = findViewById(R.id.tvMusicGenre);
        pbTrainingProgress = findViewById(R.id.pbTrainingProgress);
        btnStopTraining = findViewById(R.id.btnStopTraining);

        dataManager = new DataManager(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());
        audioManager = new AudioManager(this);
        sensorManager = new SensorManager(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Pulsante per fermare l'allenamento
        btnStopTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStopTrainingConfirmation();
            }
        });
    }

    // Prepara i dati e parametri per avviare l’allenamento
    private void prepareTrainingSession() {
        String patientCode = sessionManager.getActivePatientCode();
        patientData = dataManager.getPatientData(patientCode);
        selectedMusicGenre = dataManager.getSelectedMusicGenre();

        // Controllo validità dei dati
        if (patientData == null || patientData.getBestCadence() <= 0 || patientData.getTrainingDuration() <= 0) {
            Toast.makeText(this, R.string.invalid_training_config, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Imposta un genere musicale di default se non specificato
        if (selectedMusicGenre == null || selectedMusicGenre.isEmpty()) {
            selectedMusicGenre = "Rock"; // Default music genre
        }

        String mode = patientData.getCadenceMode();
        if ("pattern".equals(mode)) {
            rhythmPattern = patientData.getCadencePattern();
            if (rhythmPattern == null || rhythmPattern.isEmpty()) {
                Toast.makeText(this, "Missing cadence pattern", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            targetCadence = patientData.getBestCadence();
            // Calcola intervallo ritmo in ms basato sulla cadenza
            // Cadenza è passi al minuto, quindi va convertito in intervallo tra passi
            rhythmInterval = (long)((float) ((float)60000 / targetCadence));
        }

        // Imposta i parametri per la sessione
        trainingDurationMs = TimeUnit.MINUTES.toMillis(patientData.getTrainingDuration());
        remainingTimeMs = trainingDurationMs;

        // Aggiorna UI
        tvTargetCadence.setText(getString(R.string.target_cadence_value, targetCadence));
        tvCurrentCadence.setText(getString(R.string.current_cadence_value, 0.0f));
        tvMusicGenre.setText(getString(R.string.music_genre_value, selectedMusicGenre));

        // Crea oggetto per la sessione corrente
        currentSession = new TrainingSession();
        currentSession.setStartTime(System.currentTimeMillis());
        currentSession.setTargetCadence(targetCadence);
        currentSession.setDuration(patientData.getTrainingDuration());
        currentSession.setMusicGenre(selectedMusicGenre);

        // Show countdown and start training
        startCountdown(); // Mostra conto alla rovescia prima di iniziare
    }

    // Conto alla rovescia iniziale
    private void startCountdown() {
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                Toast.makeText(TrainingActivity.this,
                        getString(R.string.starting_in, secondsLeft),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                startTrainingSession();// Inizia sessione effettiva
            }
        }.start();
    }

    // Inizia la sessione di allenamento
    private void startTrainingSession() {
        isTrainingActive = true;

        // Avvia musica di sottofondo
        audioManager.playBackgroundMusic(selectedMusicGenre);

        // Inizia la misurazione della cadenza tramite sensori
        sensorManager.startMeasuring(cadence -> runOnUiThread(() -> {
            currentCadence = cadence;
            tvCurrentCadence.setText(getString(R.string.current_cadence_value, currentCadence));
            provideCadenceFeedback(); //vibrazione se la cadenza è errata
        }));

        startRhythmSounds();     // Avvia suoni ritmici
        startTrainingTimer();    // Avvia il timer della sessione
    }

    // Avvio dei suoni ritmici periodici
    private void startRhythmSounds() {
        SharedPreferences prefs = getSharedPreferences("mSWAYPrefs", MODE_PRIVATE);
        String rhythmName = prefs.getString("selected_rhythm", "clap_downbeat"); // 🆕 default
        float rhythmVolume = prefs.getFloat("rhythm_volume", 1.0f); // 🆕 0.0 to 1.0

        int resId = getResources().getIdentifier(rhythmName, "raw", getPackageName());

        if ("pattern".equals(patientData.getCadenceMode())) {
            intervalIndex = 0;
            rhythmRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isTrainingActive || intervalIndex >= rhythmPattern.size()) return;

                    MediaPlayer rhythmPlayer = MediaPlayer.create(TrainingActivity.this, resId);
                    if (rhythmPlayer != null) {
                        rhythmPlayer.setVolume(rhythmVolume, rhythmVolume);
                        rhythmPlayer.setOnCompletionListener(MediaPlayer::release);
                        rhythmPlayer.start();
                    }

                    long nextInterval = rhythmPattern.get(intervalIndex);
                    intervalIndex++;
                    rhythmHandler.postDelayed(this, nextInterval);
                }
            };
        } else {
            rhythmRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("prova ritmo 1", "isTrainingActive:" + isTrainingActive);
                    if (isTrainingActive) {
                        Log.d("prova ritmo 2", "sono qui:" + rhythmInterval);
                        MediaPlayer rhythmPlayer = MediaPlayer.create(TrainingActivity.this, resId);
                        if (rhythmPlayer != null) {
                            rhythmPlayer.setVolume(rhythmVolume, rhythmVolume); // 🆕 Apply volume
                            rhythmPlayer.setOnCompletionListener(MediaPlayer::release);
                            rhythmPlayer.start();
                        }
                        rhythmHandler.postDelayed(this, rhythmInterval);
                    }
                }
            };
        }

        rhythmHandler.post(rhythmRunnable);
    }
                

    // Vibrazione se la cadenza è distante dal target
    private void provideCadenceFeedback() {
        // Calcola la differenza tra cadenza attuale e obbiettivo
        float cadenceDiff = Math.abs(currentCadence - targetCadence);
        float percentDiff = (cadenceDiff / targetCadence) * 100;

        // Fornisce feedback se cadenza è troppo distante da obbiettivo
        if (percentDiff > 15 && vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(200);// Vibrazione di 200ms
        }
    }

    // Avvia il conto alla rovescia dell’allenamento
    private void startTrainingTimer() {
        pbTrainingProgress.setMax(100);

        trainingTimer = new CountDownTimer(trainingDurationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeMs = millisUntilFinished;

                // Aggiorna timer visivo
                String timeDisplay = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                );
                tvTimer.setText(timeDisplay);

                // Aggiorna barra di progresso
                int progress = (int) (((trainingDurationMs - millisUntilFinished) * 100) / trainingDurationMs);
                pbTrainingProgress.setProgress(progress);
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                pbTrainingProgress.setProgress(100);
                completeTrainingSession();// Fine sessione
            }
        };

        trainingTimer.start();
    }

    // Completa e salva la sessione di allenamento
    private void completeTrainingSession() {
        isTrainingActive = false;

        // Ferma tutti i processi in atto
        stopTrainingProcesses();

        // Salva i dati
        currentSession.setEndTime(System.currentTimeMillis());
        currentSession.setAverageCadence(currentCadence);
        currentSession.setCompleted(true);
        dataManager.saveTrainingSession(currentSession);

        // Mostra dialog di completamento
        new AlertDialog.Builder(this)
                .setTitle(R.string.training_complete)
                .setMessage(R.string.training_complete_message)
                .setPositiveButton(R.string.ok, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    // Ferma sessione manualmente
    private void stopTrainingSession() {
        if (isTrainingActive) {
            isTrainingActive = false;

            // Ferma tutti i processi in atto
            stopTrainingProcesses();

            // Salva dati parziali
            currentSession.setEndTime(System.currentTimeMillis());
            currentSession.setAverageCadence(currentCadence);
            currentSession.setCompleted(false);
            dataManager.saveTrainingSession(currentSession);

            finish();
        }
    }

    // Ferma timer, audio, sensori, ritmo
    private void stopTrainingProcesses() {
        // Stop timer
        if (trainingTimer != null) {
            trainingTimer.cancel();
        }

        // Ferma suoni ritmici
        rhythmHandler.removeCallbacks(rhythmRunnable);

        // Ferma il monitoraggio con sensori
        sensorManager.stopMeasuring();

        // Ferma musica di sottofondo
        audioManager.stopBackgroundMusic();
    }

    // Mostra conferma per fermare sessione
    private void showStopTrainingConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.stop_training)
                .setMessage(R.string.stop_training_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> stopTrainingSession())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (isTrainingActive) {
            showStopTrainingConfirmation();// Blocca back se sessione attiva
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTrainingProcesses();// Ferma tutto se activity viene chiusa
    }
}
