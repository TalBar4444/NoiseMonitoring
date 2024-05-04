package com.myapps.mobilesecurityapp.model;

import android.util.Log;

import com.myapps.mobilesecurityapp.utils.RecordingUtil;

import java.io.Serializable;

public class Audio implements Serializable {
    private String genericName; // Unique identifier for the audio (e.g., audio_01)
    private int minVolume;
    private int maxVolume;
    private int averageVolume;
    private String duration;
    private String dateRecorded;
    private double noiseLevel;

    public Audio() {}

    ////////////////////////////////// getters and setter ////////////////////////////////////////

    public String getGenericName() {
        return genericName;
    }

    public Audio setGenericName() {
        this.genericName = RecordingUtil.generateUniqueFileName();
        return this;
    }

    public int getMinVolume() {
        return minVolume;
    }

    public Audio setMinVolume(int minVolume) {
        this.minVolume = minVolume;
        return this;
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    public Audio setMaxVolume(int maxVolume) {
        this.maxVolume = maxVolume;
        return this;
    }

    public double getCurrentNoiseLevel() {
        return noiseLevel;
    }

    public Audio setCurrentNoiseLevel(double noiseLevel) {
        this.noiseLevel = noiseLevel;
        return this;
    }

    public int getAverageVolume() {
        return averageVolume;
    }

    public Audio setAverageVolume(int averageVolume) {
        this.averageVolume = averageVolume;
        return this;
    }

    public String getDuration() {
        return duration;
    }

    public Audio setDuration(long durationInMilliSec) {
        this.duration = getFormattedDuration(durationInMilliSec);
        return this;
    }

    public String getDateRecorded() {
        return dateRecorded;
    }

    public Audio setDateRecorded(String dateRecorded) {
        this.dateRecorded = dateRecorded;
        return this;
    }

    ////////////////////////////////// additional functions ////////////////////////////////////////

    public String getFormattedDuration(long durationInMilliSec) {

        int durationInSeconds = (int) (durationInMilliSec / 1000);  // Convert milliseconds to seconds

        int hours = durationInSeconds / 3600;
        int remainingSeconds = durationInSeconds % 3600;
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;

        String formattedTime = null;
        if(hours == 0){
            if(minutes == 0)
                formattedTime = seconds + " seconds";
            else{ //minutes > 0
                if(seconds == 0)
                    formattedTime = minutes + " minutes";
                else //seconds > 0
                    formattedTime = minutes + " minutes, " + seconds + " seconds";
            }
        }
        else { // hours > 0
            if(minutes > 0) {
                if (seconds > 0)
                    formattedTime = hours + " hours, " + minutes + " minutes, " + seconds + " seconds";
                else //seconds=0
                    formattedTime = hours + " hours, " + minutes + " minutes";
            }
            else { //minutes = 0
                if (seconds > 0)
                    formattedTime = hours + " hours, " + seconds + " seconds";
                else // seconds = 0
                    formattedTime = hours + " hours";
            }

        }
        return formattedTime;
    }

    public String toString() {
        // Return a string representation of the record
        return "Audio [genericName=" + genericName + ", averageVolume=" + averageVolume +
                ", maxVolume=" + maxVolume + ", minVolume=" + minVolume + ", duration=" + duration +
                ", dateRecorded=" + dateRecorded + "]";
    }
}
