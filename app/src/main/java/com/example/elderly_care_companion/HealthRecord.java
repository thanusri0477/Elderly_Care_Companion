package com.example.elderly_care_companion;

public class HealthRecord {
    private String userId, bp, sugarLevel, temperature, pulseRate, medications, doseLevels, medicationTime, status, date, caregiverId;

    public HealthRecord(String userId, String bp, String sugarLevel, String temperature, String pulseRate,
                        String medications, String doseLevels, String medicationTime, String status, String date, String caregiverId) {
        this.userId = userId;
        this.bp = bp;
        this.sugarLevel = sugarLevel;
        this.temperature = temperature;
        this.pulseRate = pulseRate;
        this.medications = medications;
        this.doseLevels = doseLevels;
        this.medicationTime = medicationTime;
        this.status = status;
        this.date = date;
        this.caregiverId = caregiverId;
    }

    public String getUserId() { return userId; }
    public String getBp() { return bp; }
    public String getSugarLevel() { return sugarLevel; }
    public String getTemperature() { return temperature; }
    public String getPulseRate() { return pulseRate; }
    public String getMedications() { return medications; }
    public String getDoseLevels() { return doseLevels; }
    public String getMedicationTime() { return medicationTime; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getCaregiverId() { return caregiverId; }
}
