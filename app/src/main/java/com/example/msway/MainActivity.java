package com.example.msway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.msway.utils.DataManager;
import com.example.msway.utils.SessionManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Button btnPatient;
    private Button btnClinician;
    private SessionManager sessionManager;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize components
        initializeComponents();

        // Create data directories if they don't exist
        setupDataDirectories();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(getApplicationContext());
        dataManager = new DataManager(getApplicationContext());

        btnPatient = findViewById(R.id.btnPatient);
        btnClinician = findViewById(R.id.btnClinician);

        btnPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PatientActivity.class);
                startActivity(intent);
            }
        });

        btnClinician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupDataDirectories() {
        // Create app data directories
        File dataDir = new File(getFilesDir(), "mSWAY_data");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        // Create subdirectories for labels, protocols, and subjects
        File labelsDir = new File(dataDir, "labels");
        File protocolsDir = new File(dataDir, "protocols");
        File subjectsDir = new File(dataDir, "subjects");

        if (!labelsDir.exists()) labelsDir.mkdir();
        if (!protocolsDir.exists()) protocolsDir.mkdir();
        if (!subjectsDir.exists()) subjectsDir.mkdir();

        // Create raw data and processed data directories inside subjects
        File rawDataDir = new File(subjectsDir, "raw_data");
        File processedDataDir = new File(subjectsDir, "processed_data");

        if (!rawDataDir.exists()) rawDataDir.mkdir();
        if (!processedDataDir.exists()) processedDataDir.mkdir();
    }
}
