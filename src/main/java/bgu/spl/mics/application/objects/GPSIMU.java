package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {

    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;

    public GPSIMU(int currentTick){
        this.currentTick = currentTick;
        this.status = STATUS.UP;
        this.poseList = new LinkedList<>();
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public STATUS getStatus() {
        return status;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }
     public void addPose(Pose pose){
        poseList.add(pose);
     }

    public void setStatus(STATUS status) {
        this.status = status;
    }
}
