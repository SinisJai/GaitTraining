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

public class ClinicianActivity extends AppCompatActivity {
    private TextView tvTrainingDuration;
    private SeekBar sbTrainingDuration;
    private RadioGroup rgCadenceMethod;
    private RadioButton rbManualCadence;
    private RadioButton rbSensorCadence;
    private EditText etManualCadence;
    private Button btnMeasureCadence;
    private Button btnSave;
    private Button btnLogout;

    private DataManager dataManager;
    private SessionManager sessionManager;
    private SensorManager sensorManager;

    private int trainingDuration = 5; // Default 5 minutes
    private float bestCadence = 0;
    private boolean isMeasuringSensors = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_clinician);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainClinicianLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeComponents();
        loadExistingData();
    }

    private void initializeComponents() {
        tvTrainingDuration = findViewById(R.id.tvTrainingDuration);
        sbTrainingDuration = findViewById(R.id.sbTrainingDuration);
        rgCadenceMethod = findViewById(R.id.rgCadenceMethod);
        rbManualCadence = findViewById(R.id.rbManualCadence);
        rbSensorCadence = findViewById(R.id.rbSensorCadence);
        etManualCadence = findViewById(R.id.etManualCadence);
        btnMeasureCadence = findViewById(R.id.btnMeasureCadence);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);

        dataManager = new DataManager(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());
        sensorManager = new SensorManager(this);

        setupTrainingDurationControls();
        setupCadenceControls();
        setupButtons();
    }

    private void setupTrainingDurationControls() {
        sbTrainingDuration.setMin(1);
        sbTrainingDuration.setMax(30);
        sbTrainingDuration.setProgress(trainingDuration);
        updateTrainingDurationText();

        sbTrainingDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                trainingDuration = progress;
                updateTrainingDurationText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupCadenceControls() {
        rgCadenceMethod.setOnCheckedChangeListener((group, checkedId) -> updateCadenceMethod());
        updateCadenceMethod();
    }

    private void setupButtons() {
        btnMeasureCadence.setOnClickListener(v -> {
            if (!isMeasuringSensors) {
                startSensorMeasurement();
            } else {
                stopSensorMeasurement();
            }
        });

        btnSave.setOnClickListener(v -> saveSettings());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void updateTrainingDurationText() {
        tvTrainingDuration.setText(getString(R.string.training_duration_minutes, trainingDuration));
    }

    private void updateCadenceMethod() {
        boolean isManual = rbManualCadence.isChecked();
        etManualCadence.setEnabled(isManual);
        btnMeasureCadence.setEnabled(!isManual);

        if (isManual) {
            stopSensorMeasurement();
        }
    }

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

    private void stopSensorMeasurement() {
        if (isMeasuringSensors) {
            sensorManager.stopMeasuring();
            isMeasuringSensors = false;
            btnMeasureCadence.setText(R.string.measure_cadence);
        }
    }

    private void saveSettings() {
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

        PatientData patientData = new PatientData();
        patientData.setTrainingDuration(trainingDuration);
        patientData.setBestCadence(bestCadence);

        try {
            dataManager.savePatientData(patientData);
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExistingData() {
        PatientData patientData = dataManager.getPatientData();
        if (patientData != null) {
            trainingDuration = patientData.getTrainingDuration();
            bestCadence = patientData.getBestCadence();

            sbTrainingDuration.setProgress(trainingDuration);
            updateTrainingDurationText();

            if (bestCadence > 0) {
                etManualCadence.setText(String.valueOf(bestCadence));
            }
        }
    }

    private void logout() {
        stopSensorMeasurement();
        sessionManager.setLoggedIn(false);
        sessionManager.setUsername("");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSensorMeasurement();
    }
}
