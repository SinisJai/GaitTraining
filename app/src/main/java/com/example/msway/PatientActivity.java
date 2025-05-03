package com.example.msway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.msway.models.PatientData;
import com.example.msway.utils.DataManager;
import com.example.msway.utils.SessionManager;

public class PatientActivity extends AppCompatActivity {

    private Button btnStartTraining;
    private Button btnSelectMusic;
    private Button btnBack;
    private TextView tvTrainingDuration;
    private TextView tvBestCadence;

    private DataManager dataManager;
    private SessionManager sessionManager;
    private PatientData patientData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPatientLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIWithPatientData();
    }

    private void initializeComponents() {
        btnStartTraining = findViewById(R.id.btnStartTraining);
        btnSelectMusic = findViewById(R.id.btnSelectMusic);
        btnBack = findViewById(R.id.btnBack);
        tvTrainingDuration = findViewById(R.id.tvTrainingDuration);
        tvBestCadence = findViewById(R.id.tvBestCadence);

        dataManager = new DataManager(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());

        btnStartTraining.setOnClickListener(v -> startTrainingSession());

        btnSelectMusic.setOnClickListener(v -> {
            Intent intent = new Intent(PatientActivity.this, MusicSelectionActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void updateUIWithPatientData() {
        String patientCode = sessionManager.getActivePatientCode();
        if (patientCode == null) {
            Toast.makeText(this, "No active patient assigned. Clinician setup required.", Toast.LENGTH_LONG).show();
            return;
        }

        patientData = dataManager.getPatientData(patientCode);
        if (patientData != null) {
            tvTrainingDuration.setText(getString(R.string.training_duration_value, patientData.getTrainingDuration()));

            if (patientData.getBestCadence() > 0) {
                tvBestCadence.setText(getString(R.string.best_cadence_value, patientData.getBestCadence()));
                btnStartTraining.setEnabled(true);
            } else {
                tvBestCadence.setText(R.string.no_cadence_recorded);
                btnStartTraining.setEnabled(false);
            }
        } else {
            tvTrainingDuration.setText(R.string.no_training_duration_set);
            tvBestCadence.setText(R.string.no_cadence_recorded);
            btnStartTraining.setEnabled(false);
            Toast.makeText(this, "Patient data not found for code: " + patientCode, Toast.LENGTH_LONG).show();
        }
    }

    private void startTrainingSession() {
        if (patientData != null && patientData.getBestCadence() > 0 && patientData.getTrainingDuration() > 0) {
            Intent intent = new Intent(PatientActivity.this, TrainingActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Setup incomplete. Please contact clinician.", Toast.LENGTH_SHORT).show();
        }
    }
}
