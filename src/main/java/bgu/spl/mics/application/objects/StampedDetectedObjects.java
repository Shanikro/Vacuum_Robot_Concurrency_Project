package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {

    private int time;
    private List<DetectedObject> detectedObjects;

    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects){

        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    public int getTime(){
        return time;
    }

    public List<DetectedObject> getDetectedObjects(){
        return detectedObjects;
    }

}
