package com.example.msway.models;


import java.io.Serializable;

public class TrainingSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private long startTime;
    private long endTime;
    private int duration; // in minutes
    private float targetCadence;
    private float averageCadence;
    private String musicGenre;
    private boolean completed = true;

    public TrainingSession() {
        // Default constructor required for serialization
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getTargetCadence() {
        return targetCadence;
    }

    public void setTargetCadence(float targetCadence) {
        this.targetCadence = targetCadence;
    }

    public float getAverageCadence() {
        return averageCadence;
    }

    public void setAverageCadence(float averageCadence) {
        this.averageCadence = averageCadence;
    }

    public String getMusicGenre() {
        return musicGenre;
    }

    public void setMusicGenre(String musicGenre) {
        this.musicGenre = musicGenre;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "TrainingSession{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                ", targetCadence=" + targetCadence +
                ", averageCadence=" + averageCadence +
                ", musicGenre='" + musicGenre + '\'' +
                ", completed=" + completed +
                '}';
    }
}
