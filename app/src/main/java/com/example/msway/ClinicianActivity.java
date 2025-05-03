package com.example.msway;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.msway.models.PatientData;
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
    private EditText etManualCadence;
    private Button btnMeasureCadence;
    private Button btnSave;
    private Button btnLogout;
    private EditText etPatientCode; // Added patient code field

    // Manager per i dati, sessioni e sensori
    private DataManager dataManager;
    private SessionManager sessionManager;
    private SensorManager sensorManager;

    // Variabili di stato
    private int trainingDuration = 30; // Default 30 minuti
    private float bestCadence = 0;
    private boolean isMeasuringSensors = false;

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

        initializeComponents();// Inizializza la UI e i manager
        loadExistingData(); // Carica i dati precedentemente salvati
    }

    // Metodo per inizializzare componenti UI e logica
    private void initializeComponents() {
        // Collega i componenti della UI
        tvTrainingDuration = findViewById(R.id.tvTrainingDuration);
        sbTrainingDuration = findViewById(R.id.sbTrainingDuration);
        rgCadenceMethod = findViewById(R.id.rgCadenceMethod);
        rbManualCadence = findViewById(R.id.rbManualCadence);
        rbSensorCadence = findViewById(R.id.rbSensorCadence);
        etManualCadence = findViewById(R.id.etManualCadence);
        btnMeasureCadence = findViewById(R.id.btnMeasureCadence);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        etPatientCode = findViewById(R.id.etPatientCode); // Added patient code field initialization

        // Inizializza i manager
        dataManager = new DataManager(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());
        sensorManager = new SensorManager(this);

        // Setup delle funzionalità
        setupTrainingDurationControls();
        setupCadenceControls();
        setupButtons();
    }

    // Gestione della SeekBar per la durata dell'allenamento
    private void setupTrainingDurationControls() {
        sbTrainingDuration.setMin(1);
        sbTrainingDuration.setMax(30);
        sbTrainingDuration.setProgress(trainingDuration);
        updateTrainingDurationText();//Aggiorna il testo visibile

        sbTrainingDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                trainingDuration = progress;
                updateTrainingDurationText();//Mostra nuovo valore
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Logica per il metodo di cadenza scelto: manuale o tramite sensore
    private void setupCadenceControls() {
        rgCadenceMethod.setOnCheckedChangeListener((group, checkedId) -> updateCadenceMethod());
        updateCadenceMethod();
    }

    // Imposta gli eventi click per i pulsanti
    private void setupButtons() {
        btnMeasureCadence.setOnClickListener(v -> {
            if (!isMeasuringSensors) {
                startSensorMeasurement(); // Avvia misurazione
            } else {
                stopSensorMeasurement(); // Ferma misurazione
            }
        });

        btnSave.setOnClickListener(v -> saveSettings()); // Salva configurazione
        btnLogout.setOnClickListener(v -> logout()); //Effettua logout
    }

    private void updateTrainingDurationText() {
        tvTrainingDuration.setText(getString(R.string.training_duration_minutes, trainingDuration));
    }

    // Abilita/disabilita campi in base al metodo di cadenza scelto
    private void updateCadenceMethod() {
        boolean isManual = rbManualCadence.isChecked();
        etManualCadence.setEnabled(isManual);
        btnMeasureCadence.setEnabled(!isManual);

        if (isManual) {
            stopSensorMeasurement(); // Ferma i sensori se si passa al manuale
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
            Toast.makeText(this, R.string.enter_patient_code, Toast.LENGTH_SHORT).show(); // Added error message
            return;
        }
        if (rbManualCadence.isChecked()) {
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

        // Crea oggetto con i dati del paziente e salva
        PatientData data = new PatientData();
        data.setPatientCode(patientCode);
        data.setTrainingDuration(trainingDuration);
        data.setBestCadence(bestCadence);

        dataManager.savePatientData(data);
        sessionManager.setActivePatientCode(patientCode);

        Toast.makeText(this, "Settings saved for " + patientCode, Toast.LENGTH_SHORT).show();
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

    // Metodo di logout: azzera sessione e chiude l’attività
    private void logout() {
        stopSensorMeasurement();
        sessionManager.setLoggedIn(false);
        sessionManager.setUsername("");
        finish();
    }

    // Ferma eventuali sensori alla distruzione dell’attività
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSensorMeasurement();
    }
}
