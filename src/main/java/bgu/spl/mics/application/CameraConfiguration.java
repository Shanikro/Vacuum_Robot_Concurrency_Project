package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.List;

public class CameraConfiguration {
    private int id;
    private int frequency;
    private List<StampedDetectedObjects> detectedObjects;

    // Constructor
    public CameraConfiguration(int id, int frequency, List<StampedDetectedObjects> detectedObjects) {
        this.id = id;
        this.frequency = frequency;
        this.detectedObjects = detectedObjects;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public List<StampedDetectedObjects> getDetectedObjects() {
        return detectedObjects;
    }

    // Optional: toString for debugging
    @Override
    public String toString() {
        return "CameraConfiguration{" +
                "id=" + id +
                ", frequency=" + frequency +
                ", detectedObjects=" + detectedObjects +
                '}';
    }
}
