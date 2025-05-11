package com.example.msway;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.msway.models.PatientData;
import com.example.msway.models.User;
import com.example.msway.utils.DataManager;
import com.example.msway.utils.SensorManager;
import com.example.msway.utils.SessionManager;


// Classe principale dell'attività per il clinico, permette di configurare durata e cadenza dell’allenamento
public class ClinicianActivity extends AppCompatActivity {
    // Dichiarazione dei componenti UI
    private TextView tvTrainingDuration;
    private SeekBar sbTrainingDuration;
    private RadioGroup rgCadenceMethod;
    private RadioButton rbManualCadence;
    private RadioButton rbSensorCadence;
    private RadioButton rbPatternCadence;

    private EditText etManualCadence;
    private Button btnMeasureCadence;
    private Button btnSave;
    private Button btnLogout;
    private EditText etPatientCode;

    // Manager per i dati, sessioni e sensori
    private DataManager dataManager;
    private SessionManager sessionManager;
    private SensorManager sensorManager;

    // Variabili di stato
    private int trainingDuration = 30; // Default 30 minuti
    private float bestCadence = 0;
    private boolean isMeasuringSensors = false;
    private User loggedInClinician;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);// Abilita layout edge-to-edge
        setContentView(R.layout.activity_clinician);

        // Gestione dei margini per evitare sovrapposizioni con barre di sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainClinicianLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dataManager = new DataManager(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());
        sensorManager = new SensorManager(this);
        loggedInClinician = sessionManager.getLoggedInUser();

        initializeViews();
        setupListeners();
        loadExistingData();

    }

    // Metodo per inizializzare componenti UI e logica
    private void initializeViews() {
        tvTrainingDuration = findViewById(R.id.tvTrainingDuration);
        sbTrainingDuration = findViewById(R.id.sbTrainingDuration);
        rgCadenceMethod = findViewById(R.id.rgCadenceMethod);
        rbManualCadence = findViewById(R.id.rbManualCadence);
        rbSensorCadence = findViewById(R.id.rbSensorCadence);
        rbPatternCadence = findViewById(R.id.rbPatternCadence);
        etManualCadence = findViewById(R.id.etManualCadence);
        etPatientCode = findViewById(R.id.etPatientCode);
        btnMeasureCadence = findViewById(R.id.btnMeasureCadence);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);

        sbTrainingDuration.setMin(1);
        sbTrainingDuration.setMax(30);
        sbTrainingDuration.setProgress(trainingDuration);
        updateTrainingDurationText();
    }

    private void setupListeners() {
        sbTrainingDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                trainingDuration = progress;
                updateTrainingDurationText();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        rgCadenceMethod.setOnCheckedChangeListener((group, checkedId) -> updateCadenceMethod());

        btnMeasureCadence.setOnClickListener(v -> {
            if (!isMeasuringSensors) {
                startSensorMeasurement(); //avvia misurazione
            } else {
                stopSensorMeasurement(); //ferma misurazione
            }
        });

        btnSave.setOnClickListener(v -> saveSettings());// Salva configurazione

        btnLogout.setOnClickListener(v -> {
            stopSensorMeasurement();
            sessionManager.setLoggedIn(false);
            sessionManager.setUsername(""); // optional clean-up
            finish();
        });

        updateCadenceMethod(); //set correct field state on startup
    }

    private void updateTrainingDurationText() {
        tvTrainingDuration.setText(getString(R.string.training_duration_minutes, trainingDuration));
    }

    // Abilita/disabilita campi in base al metodo di cadenza scelto
    private void updateCadenceMethod() {
        if (rbManualCadence.isChecked()) {
            etManualCadence.setEnabled(true);
            btnMeasureCadence.setEnabled(false);
            stopSensorMeasurement();
        } else if (rbSensorCadence.isChecked()) {
            etManualCadence.setEnabled(false);
            btnMeasureCadence.setEnabled(true);
        } else if (rbPatternCadence.isChecked()) {
            etManualCadence.setEnabled(false);
            btnMeasureCadence.setEnabled(false);
            stopSensorMeasurement();
        }
    }

    // Avvia la misurazione della cadenza tramite sensori
    private void startSensorMeasurement() {
        if (!sensorManager.checkSensorsAvailable()) {
            Toast.makeText(this, R.string.sensors_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        isMeasuringSensors = true;
        btnMeasureCadence.setText(R.string.stop_measuring);

        sensorManager.startMeasuring(cadence -> runOnUiThread(() -> {
            bestCadence = cadence;
            Toast.makeText(ClinicianActivity.this,
                    getString(R.string.cadence_measured, bestCadence),
                    Toast.LENGTH_SHORT).show();
        }));

        Toast.makeText(this, R.string.measuring_cadence, Toast.LENGTH_SHORT).show();
    }

    // Ferma la misurazione dei sensori
    private void stopSensorMeasurement() {
        if (isMeasuringSensors) {
            sensorManager.stopMeasuring();
            isMeasuringSensors = false;
            btnMeasureCadence.setText(R.string.measure_cadence);
        }
    }

    // Salva la configurazione impostata dal clinico
    private void saveSettings() {

        String patientCode = etPatientCode.getText().toString().trim();

        if (patientCode.isEmpty()) {
            Toast.makeText(this, R.string.enter_patient_code, Toast.LENGTH_SHORT).show();
            return;
        }

        // Crea oggetto con i dati del paziente e salva
        PatientData data = new PatientData();
        data.setPatientCode(patientCode);
        data.setTrainingDuration(trainingDuration);
        data.setLastModifiedBy(loggedInClinician != null ? loggedInClinician.getUsername() : "unknown");

        String cadenceMode;
        if (rbPatternCadence.isChecked()) {
            cadenceMode = "pattern";
            List<Long> pattern = dataManager.getCadencePatternForPatient(patientCode);
            if (pattern == null || pattern.isEmpty()) {
                Toast.makeText(this, "No recorded pattern found for this patient", Toast.LENGTH_SHORT).show();
                return;
            }
            data.setCadencePattern(pattern);
            bestCadence = 60; // dummy fallback to avoid 0 division later
        } else if (rbSensorCadence.isChecked()) {
            cadenceMode = "sensor";
        } else {
            cadenceMode = "manual";
            String cadenceStr = etManualCadence.getText().toString().trim();
            if (cadenceStr.isEmpty()) {
                Toast.makeText(this, R.string.enter_cadence, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                bestCadence = Float.parseFloat(cadenceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.invalid_cadence, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (bestCadence <= 0) {
            Toast.makeText(this, R.string.invalid_cadence, Toast.LENGTH_SHORT).show();
            return;
        }

        data.setCadenceMode(cadenceMode);
        data.setBestCadence(bestCadence);

        dataManager.savePatientData(data);
        sessionManager.setActivePatientCode(patientCode);

        Toast.makeText(this, "Settings saved by " + data.getLastModifiedBy(), Toast.LENGTH_SHORT).show();
    }

    // Carica eventuali dati salvati in precedenza
    private void loadExistingData() {
        String patientCode = sessionManager.getActivePatientCode();
        if (patientCode != null) {
            PatientData data = dataManager.getPatientData(patientCode);
            if (data != null) {
                trainingDuration = data.getTrainingDuration();
                bestCadence = data.getBestCadence();
                etPatientCode.setText(data.getPatientCode());
                sbTrainingDuration.setProgress(trainingDuration);
                updateTrainingDurationText();
                etManualCadence.setText(String.valueOf(bestCadence));
            }
        }
    }

    // Ferma eventuali sensori alla distruzione dell’attività
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSensorMeasurement();
    }
}
