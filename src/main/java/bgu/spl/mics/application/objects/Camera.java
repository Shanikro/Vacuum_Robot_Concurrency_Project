package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {

    private int id;
    private int frequency;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectsList;

    public Camera(int id, int frequency) {

    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public StampedDetectedObjects detectObjects(int tick) {
        for (StampedDetectedObjects o : detectedObjectsList){
            if(o.getTime() == tick){
                return o;
            }
        }
        return null;
    }

    public boolean isUp() {
        return status == STATUS.UP;
    }

    public boolean isDown() {
        return status == STATUS.DOWN;
    }

    public boolean isError() {
        return status == STATUS.ERROR;
    }


}
