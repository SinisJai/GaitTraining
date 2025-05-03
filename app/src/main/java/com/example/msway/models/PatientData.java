package com.example.msway.models;

import java.io.Serializable;

public class PatientData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String patientCode;
    private int trainingDuration = 5; // Default 5 minutes
    private float bestCadence = 0;

    public PatientData() {
        // Default constructor required for serialization
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public int getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(int trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public float getBestCadence() {
        return bestCadence;
    }

    public void setBestCadence(float bestCadence) {
        this.bestCadence = bestCadence;
    }

    @Override
    public String toString() {
        return "PatientData{" +
                "patientCode='" + patientCode + '\'' +
                ", trainingDuration=" + trainingDuration +
                ", bestCadence=" + bestCadence +
                '}';
    }
}