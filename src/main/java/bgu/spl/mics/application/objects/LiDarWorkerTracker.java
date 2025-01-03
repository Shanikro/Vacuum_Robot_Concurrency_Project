package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

    private int currentTick;
    private int stampedPointsUntilFinish;
    private TrackedObject lastTrackedObject;

    public LiDarWorkerTracker(int id, int frequency){

        this.id = id;
        this.frequency =  frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new LinkedList<>();

        this.currentTick = 0;
        this.stampedPointsUntilFinish = LiDarDataBase.getInstance().getCloudPoints().size();
        this.lastTrackedObject = null;

        StatisticalFolder.getInstance().addLiDar(this); //Update statistic folder about new camera
    }

    //Getters

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

    public TrackedObject getLastTrackedObject() {
        return lastTrackedObject;
    }

    //Methods

    public void addTrackedObject(TrackedObject obj){
        lastTrackedObjects.add(obj);
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<TrackedObject> handleTick(int time) {

        currentTick = time;

        List<TrackedObject> trackedObjectsToSlam = new LinkedList<>();
        for(TrackedObject o : lastTrackedObjects){ //TODO סנכרון
            if(o.getTime() == currentTick - frequency) {
                trackedObjectsToSlam.add(o); //Add this object to the list used by Fusion Slam
                lastTrackedObjects.remove(o); //Remove the object from the last tracked object list of the lidar
            }
        }

        //If we found corresponding trackedObjects
        if (!trackedObjectsToSlam.isEmpty()) {
            //Update the count until the LiDar finish
            stampedPointsUntilFinish = stampedPointsUntilFinish - trackedObjectsToSlam.size();

            //Update the number of Tracked Objects in the Statistical Folder
            StatisticalFolder.getInstance().addTrackedObjects(trackedObjectsToSlam.size());

            //Update lastTrackedObject for a case of error in the future
            lastTrackedObject = trackedObjectsToSlam.get(trackedObjectsToSlam.size()-1);
        }

        //In case that the LiDar finish
        if(stampedPointsUntilFinish == 0 && lastTrackedObjects.isEmpty()){
            setStatus(STATUS.DOWN);
        }

        return trackedObjectsToSlam;
    }

    public List<TrackedObject> handleDetectObjects(DetectObjectsEvent detectObjectsevent) {

        StampedDetectedObjects stampedDetectedObjects = detectObjectsevent.getDetectedObjects(); //Include list of detected objects

        if (stampedDetectedObjects == null) {
            return new LinkedList<>(); // Return an empty list
        }

        int trackedTime = stampedDetectedObjects.getTime() + frequency; //Time to send as event

        List<TrackedObject> trackedObjectsToSlam = new LinkedList<>();
        //TODO סנכרון
        for (DetectedObject detectedObject : stampedDetectedObjects.getDetectedObjects()) {

            List<CloudPoint> listCoordinates = getCloudPointList(detectedObject, stampedDetectedObjects.getTime()); //Create list of coordinates (cloud point) for the corresponding Object

            //In case of LiDar error,break
            if (getStatus() == STATUS.ERROR) {
                break;
            }

            //If not error, create corresponding Tracked Object
            TrackedObject newTrackedObj = new TrackedObject(detectedObject.getId(), trackedTime, detectedObject.getDescription(), listCoordinates);

            if (trackedTime <= currentTick) {
                trackedObjectsToSlam.add(newTrackedObj); //The time Tick already passed, we can send it
            } else {
                addTrackedObject(newTrackedObj); //Add the new tracked object to the LiDAR's list, waiting for the appropriate time to send it.
            }

        }

        //If the LiDar is UP, Send the tracked objects whose time has already passed
        if (getStatus() == STATUS.UP && !trackedObjectsToSlam.isEmpty()) {

            //Update the count until the LiDar finish
            stampedPointsUntilFinish = stampedPointsUntilFinish - trackedObjectsToSlam.size();

            //Update the number of Tracked Objects in the Statistical Folder
            StatisticalFolder.getInstance().addTrackedObjects(trackedObjectsToSlam.size());

            //Update lastTrackedObject for a case of error in the future
            lastTrackedObject = trackedObjectsToSlam.get(trackedObjectsToSlam.size()-1);
        }

        //In case that the LiDar finish
        if(stampedPointsUntilFinish == 0 && lastTrackedObjects.isEmpty()){
            setStatus(STATUS.DOWN);
        }

        return trackedObjectsToSlam;
    }

    private List<CloudPoint> getCloudPointList(DetectedObject detectedObject, int detectedTime) {

        List<CloudPoint> output = new LinkedList<>();

        List<StampedCloudPoints> dataBase = LiDarDataBase.getInstance().getCloudPoints();

        for (StampedCloudPoints s : dataBase) { //Find the corresponding StampedCloudPoints According to detectedTime + frequency

            //Check ERROR id's
            if (s.getId().equals("ERROR")) {
                setStatus(STATUS.ERROR);
                break;
            }

            //If everything OK
            else if (s.getTime() == detectedTime + frequency && s.getId().equals(detectedObject.getId())) {
                for (List<Double> l : s.getCloudPoints()) { //Copy each list with x and y to a CloudPoint object
                    CloudPoint newPoint = new CloudPoint(l.get(0), l.get(1));
                    output.add(newPoint);
                }
                break;
            }
        }
        return output;
    }

}
