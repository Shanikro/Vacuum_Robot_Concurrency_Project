package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private int id;
    private int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;

    public LiDarWorkerTracker(int id, int frequency){

        this.id = id;
        this.frequency =  frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new LinkedList<>();

    }

    public int getId() {
        return id;
    }

    public STATUS getStatus() {
        return status;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public int getFrequency() {
        return frequency;
    }
    public void addTrackedObject(TrackedObject obj){
        lastTrackedObjects.add(obj);
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
}
