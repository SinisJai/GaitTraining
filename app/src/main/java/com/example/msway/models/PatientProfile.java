package com.example.msway.models;

import java.io.Serializable;

public class PatientProfile implements Serializable {
    private String patientCode;
    private PatientData patientData;

    public PatientProfile(String patientCode) {
        this.patientCode = patientCode;
        this.patientData = new PatientData();
    }

    public String getPatientCode() {
        return patientCode;
    }

    public PatientData getPatientData() {
        return patientData;
    }

    public void setPatientData(PatientData data) {
        this.patientData = data;
    }
}
