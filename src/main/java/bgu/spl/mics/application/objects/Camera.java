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

    public List<StampedDetectedObjects> detectObjects(int tick) {
        List<StampedDetectedObjects> output = new LinkedList<>();
        for (StampedDetectedObjects o : detectedObjectsList){
            if(o.getTime() == tick){
                output.add(o);
            }
        }
        return output;
    }
    public List<StampedDetectedObjects> getDetectedObjectsList(){
        return detectedObjectsList;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
}
