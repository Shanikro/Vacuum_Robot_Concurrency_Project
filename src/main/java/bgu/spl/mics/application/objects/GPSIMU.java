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

    public GPSIMU(int currentTick, List<Pose> poseList){
        this.currentTick = currentTick;
        this.status = STATUS.UP;
        this.poseList = poseList;
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

    public Pose handleTick(int time) {

        //Find the corresponding pose
        currentTick = time;
        Pose currentPose = null;
        for(Pose p : poseList){
            if(p.getTime() == currentTick){
                currentPose = p;
                break;
            }
        }

        //If no more data, shut down
        if (currentPose == null)
        {
            status = STATUS.DOWN;
        }

        return currentPose;
    }
}
