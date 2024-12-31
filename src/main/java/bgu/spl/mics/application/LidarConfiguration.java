package bgu.spl.mics.application;

public class LidarConfiguration {
    private String id;
    private int frequency;

    // Constructor
    public LidarConfiguration(String id, int frequency) {
        this.id = id;
        this.frequency = frequency;
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    // Optional: toString for debugging
    @Override
    public String toString() {
        return "LidarConfiguration{" +
                "id=" + id +
                ", frequency=" + frequency +
                '}';
    }
}
