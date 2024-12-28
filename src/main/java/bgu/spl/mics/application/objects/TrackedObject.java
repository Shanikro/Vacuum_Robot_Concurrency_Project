package bgu.spl.mics.application.objects;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private String id;
    private int time;
    private String description;
    private ArrayList<CloudPoint> coordinates;

    //TODO: change the constructor that will get coordinates
    public TrackedObject(String id, int time, String description){

        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = new ArrayList<>();

    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public ArrayList<CloudPoint> getCoordinates() {
        return coordinates;
    }
    public void addCoordinates(CloudPoint p){
        coordinates.add(p);
    }
}
