package bgu.spl.mics.application.objects;

import bgu.spl.mics.Future;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;

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

    private int currentTick;
    private int stampedObjectUntilFinish;
    private String error;
    private StampedDetectedObjects lastStampedDetectedObject;

    public Camera(int id, int frequency, List<StampedDetectedObjects> detectedObjectsList) {
        this.id = id;
        this.frequency =  frequency;
        this.status = STATUS.UP;
        this.detectedObjectsList = detectedObjectsList;

        this.currentTick = 0;
        this.stampedObjectUntilFinish = detectedObjectsList.size();
        lastStampedDetectedObject = null;

        StatisticalFolder.getInstance().addCamera(this); //Update statistic folder about new camera
    }

    //Getters

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public List<StampedDetectedObjects> getDetectedObjectsList(){
        return detectedObjectsList;
    }

    public STATUS getStatus() {
        return status;
    }

    public String getError() { return error; }

    public StampedDetectedObjects getLastStampedDetectedObject() { return lastStampedDetectedObject; }

    //Method

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public StampedDetectedObjects handleTick(int time){

        currentTick = time;
        StampedDetectedObjects detectedObjectsAtTime = null;

        //In case that the camera finish
        if(stampedObjectUntilFinish == 0){
            setStatus(STATUS.DOWN);
            return detectedObjectsAtTime;
        }

        for(StampedDetectedObjects s : detectedObjectsList){
            if(s.getTime() == currentTick - frequency) {
                detectedObjectsAtTime = s;
                break;
            }
        }

        // Check if there is objects
        if (detectedObjectsAtTime != null) {
            //Check ERROR id's
            for (DetectedObject o : detectedObjectsAtTime.getDetectedObjects()) {
                if (o.getId().equals("ERROR")) {
                    setStatus(STATUS.ERROR);
                    error = o.getDescription();
                    break;
                }
            }

            //If everything OK
            if (getStatus() == STATUS.UP) {
                stampedObjectUntilFinish--; //Update the count until the camera finish

                //Update the number of Detected Objects in the Statistical Folder
                StatisticalFolder.getInstance().addDetectedObjects(detectedObjectsAtTime.getDetectedObjects().size());

                //Update lastStampedDetectedObject for a case of error in the future
                lastStampedDetectedObject = detectedObjectsAtTime;
            }
        }

        return detectedObjectsAtTime;
    }

}


