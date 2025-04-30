package com.example.msway;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

/*
 * MainActivity.java
 * Questa è l'activity di ingresso dell'applicazione mSWAY.
 * Fornisce una schermata iniziale con due opzioni: modalità Paziente e modalità Clinico.
 */

public class MainActivity extends AppCompatActivity {
    /*
     * MainActivity estende AppCompatActivity per fornire compatibilità con le moderne versioni Android
     * e accesso alle funzionalità più recenti attraverso la support library.
     */

    // Componenti dell'UI
    private Button btnPatient; // Pulsante per accedere alla modalità paziente
    private Button btnClinician; // Pulsante per accedere alla modalità clinico

    // Manager per i dati e per i sensori
    private SessionManager sessionManager;
    private DataManager dataManager;

    /*
     * onCreate: Metodo del ciclo di vita dell’Activity chiamato alla sua creazione
     * Inizializza l'interfaccia utente e imposta lo stato iniziale dell'applicazione
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);// Abilita il supporto alla visualizzazione edge-to-edge
        setContentView(R.layout.activity_main); // Imposta il layout dell’interfaccia principale
        // Gestione dei margini
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize components
        initializeComponents();

        // Create data directories if they don't exist
        setupDataDirectories();

        Log.d("prova 1","sono qui");
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
