package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;
    private int currentTick;


    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera" + camera.getId());
        this.camera = camera;
        this.currentTick = 0;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println("Camera " + getName() + " started");

        // Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {

            currentTick = tick.getTime();

            StampedDetectedObjects detectedObjects = null;
            for(StampedDetectedObjects s : camera.getDetectedObjectsList()){
                if(s.getTime() == currentTick + camera.getFrequency()) {
                    detectedObjects = s;
                    break;
                }
            }
            // Check if there is objects and the camera is on
            if (detectedObjects != null && camera.getStatus()== STATUS.UP) {
                    // Send event with detected objects
                    Future<Boolean> futureObject = (Future<Boolean>) sendEvent(new DetectObjectsEvent(getName(), detectedObjects));
                    System.out.println("Camera" + getName() + " send detected objects event");
            }
            //TODO: check what we need to do with the future

                //updates in the object - לפי מה שלוטם אמר העדכון אמור להיות באובייקט עצמו
//                    if(futureObject.get(tick.getDuration()-tick.getTime(), TimeUnit.MILLISECONDS)) {
//                        StatisticalFolder.updateDetectedObjects(camera.getDetectedObjectsCount());
//                    }

            // Handle errors
            if (!(camera.getStatus()== STATUS.UP)) {
                System.out.println("Sender " + getName() + " stopped");
                sendBroadcast(new TerminatedBroadcast(""+camera.getId()));
                terminate();
                // maybe we need another condition that checks if the status is ERROR
            }
            //}
        });
        //TODO: need to deal with the fact that the camera need to terminated when it finish to detect objects

        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            camera.setStatus(STATUS.DOWN);
            System.out.println("Camera " + camera.getId() + " stopped");
            terminate();
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            camera.setStatus(STATUS.ERROR);

        });
    }

}
