package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.Simulation.JsonOutputErrorGenerator;
import bgu.spl.mics.application.Simulation.JsonOutputGenerator;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    private List<LandMark> landMarks;
    private List<Pose> poses;

    private final Map<Integer, List<TrackedObject>> pendingTrackedObjects; //A data structure that temporarily stores objects whose corresponding Pose not arrived yet.


    //Private constructor
    private FusionSlam() {
        landMarks = new ArrayList<>();
        poses = new ArrayList<>();

        this.pendingTrackedObjects = new ConcurrentHashMap<>();
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
    public void addLandMark(LandMark landMark) {
        landMarks.add(landMark);
    }
    public void addPose(Pose pose){
        poses.add(pose);
    }

    public void handleRegister() {
        StatisticalFolder.getInstance().incrementSensorsInAction();
    }

    public void handleTrackedObjects(TrackedObjectsEvent trackedObjectsEvent) {

        int time = trackedObjectsEvent.getTrackedObjects().get(0).getTime(); //Check the time that tracked
        Pose matchedPose = getPoseByTime(time);

        //In case the corresponding Pose has not appeared yet.
        if (matchedPose == null){
            pendingTrackedObjects.put(time, trackedObjectsEvent.getTrackedObjects()); //Save the objects for later
        }

        //In case the Pose already appear, update the global Map.
        else{
            for (TrackedObject object : trackedObjectsEvent.getTrackedObjects()) {
                updateMap(object, matchedPose);
            }
        }

    }

    public void handlePose(PoseEvent poseEvent) {
        int time = poseEvent.getPose().getTime(); //Pose time
        addPose(poseEvent.getPose()); //Add Pose to the pose list of FusionSlam

        //Check if there is TrackedObjects that waiting for the pose
        if (pendingTrackedObjects.containsKey(time)) {
            List<TrackedObject> matchedTrackedObjects = pendingTrackedObjects.remove(time); //Remove them
            for (TrackedObject object : matchedTrackedObjects) {
                updateMap(object, poseEvent.getPose());
            }
        }
    }

    public void handleTerminate() {
        StatisticalFolder.getInstance().decrementSensorsInAction();
    }


    public void makeOutputErrorJson(Object object) {
        JsonOutputErrorGenerator outputData = new JsonOutputErrorGenerator(object,poses,new StatisticalFolderAndLandmarks(StatisticalFolder.getInstance(),landMarks));
        outputData.create();
    }

    public void makeOutputJson() {
        JsonOutputGenerator outputData = new JsonOutputGenerator(landMarks);
        outputData.create();
    }


    /**
     * Manages the addition of new landmarks to the map.
     */
    public void updateMap(TrackedObject trackedObject, Pose pose) {
        List<CloudPoint> updatedPoints = new LinkedList<>();

        for (CloudPoint point : trackedObject.getCoordinates()){ //For each point, calculate its relative location from the pose
            updatedPoints.add(calculatePoint(point, pose));
        }

        boolean updated = false;
        for(LandMark landMark : landMarks) {
            if (landMark.getId().equals(trackedObject.getId())) { //In case the landMark is already in the global map
                List<CloudPoint> mergedList = mergeLists(landMark.getCoordinates(), updatedPoints); //Calculate the merge coordinates
                landMark.setCoordinates(mergedList); //Update the landMark coordinates
                updated = true;
                break;
            }
        }

        if (!updated){ //In case the landMark is not in the global map
            //Increase the landmarks in the Statistic Folder by 1
            StatisticalFolder.getInstance().incrementLandMarks();

            //Add new landmark to the global map
            addLandMark(new LandMark(trackedObject.getId(), trackedObject.getDescription(), updatedPoints));
        }
    }


    /**
     * If the landmark already exists in the global map, this method calculates the new coordinates as requested.
     */
    private List<CloudPoint> mergeLists(List<CloudPoint> prevList, List<CloudPoint> newList) {
        List<CloudPoint> output = new LinkedList<>();
        int minSize = Math.min(prevList.size(), newList.size());

        for (int i = 0; i < minSize; i++) {
            output.add(makeAverage(prevList.get(i), newList.get(i)));
        }

        if (prevList.size() > newList.size()) {
            output.addAll(prevList.subList(minSize, prevList.size()));
        } else {
            output.addAll(newList.subList(minSize, newList.size()));
        }
        return output;
    }

    /**
     * Calculates the new CloudPoint considering the LiDAR cloud point and the robot's pose as requested.
     */
    private CloudPoint calculatePoint(CloudPoint point, Pose pose) {
        double xLocal = point.getX();
        double yLocal = point.getY();

        float xRobot = pose.getX();
        float yRobot = pose.getY();
        float yawDegrees = pose.getYaw();

        double yawRadians = Math.toRadians(yawDegrees);

        double cosYaw = Math.cos(yawRadians);
        double sinYaw = Math.sin(yawRadians);

        double xGlobal = (cosYaw * xLocal - sinYaw * yLocal) + xRobot;
        double yGlobal = (sinYaw * xLocal + cosYaw * yLocal) + yRobot;

        return new CloudPoint(xGlobal, yGlobal);
    }

    /**
     * If the landmark already exists in the global map, this method calculates the average between the old and new CloudPoints.
     */
    private CloudPoint makeAverage(CloudPoint last, CloudPoint newC){
        return new CloudPoint((last.getX()+newC.getX())/2,(last.getY()+newC.getY())/2);
    }

}
