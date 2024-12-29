package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    private List<LandMark> landMarks;
    private List<Pose> poses;

    //Private constructor
    private FusionSlam() {
        landMarks = new ArrayList<>();
        poses = new ArrayList<>();
    }

    //Internal static class that holds the Singleton
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }
    /**
     * Returns the single instance of FusionSlam.
     * @return Singleton instance of FusionSlam
     */
    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }


    //Getters
    public List<LandMark> getLandMarks(){
        return landMarks;
    }

    public List<TrackedObjectsEvent> getTrackedObjects(){
        return trackedObjects;
    }

    public Pose getPoseByTime(int time) {
        Pose output = null;
        for (Pose p : poses){
            if(p.getTime() == time) {
                output = p;
                break;
            }
        }
        return output;
    }


    //Methods
    public void addLandMark(LandMark landMark){
        landMarks.add(landMark);
    } //TODO:לבדוק אם לא קיים כבר

    public void addPose(Pose pose){
        poses.add(pose);
    }

    public void updateMap(TrackedObject trackedObject, Pose pose) { //TODO

    }
}
