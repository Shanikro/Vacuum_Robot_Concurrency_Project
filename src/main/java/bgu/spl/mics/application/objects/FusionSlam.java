package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import java.util.ArrayList;
import java.util.LinkedList;
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

    public void updateMap(TrackedObject trackedObject, Pose pose) {
        List<CloudPoint> updatedPoints = new LinkedList<>();

    public void updateMap(TrackedObject trackedObject, Pose pose) {

        for (CloudPoint point : trackedObject.getCoordinates()){
            updatedPoints.add(calculatePoint(point, pose));
        }

        boolean updated = false;
        for(LandMark landMark : landMarks) {
            if (landMark.getId().equals(trackedObject.getId())) {
                List<CloudPoint> mergedList = mergeLists(landMark.getCoordinates(), updatedPoints);
                landMark.setCoordinates(mergedList);
                updated = true;
                break;
            }
        }
        if (!updated){
            addLandMark(new LandMark(trackedObject.getId(), trackedObject.getDescription(), updatedPoints));
        }
    }

    private List<CloudPoint> mergeLists(List<CloudPoint> prevList, List<CloudPoint> newList) {
        List<CloudPoint> output = new LinkedList<>();
        int index = 0;
        while (prevList.get(index) != null && newList.get(index) != null){
            output.add(makeAverage(prevList.get(index), newList.get(index)));
            index++;
        }
        if(prevList.size() > newList.size()){
            while (prevList.get(index) != null)
                output.add(prevList.get(index));
        }
        else {
            while (newList.get(index) != null)
                output.add(newList.get(index));
        }
        return output;
    }

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


    private CloudPoint makeAverage(CloudPoint last, CloudPoint newC){
        return new CloudPoint((last.getX()+newC.getX())/2,(last.getY()+newC.getY())/2);
    }

}
