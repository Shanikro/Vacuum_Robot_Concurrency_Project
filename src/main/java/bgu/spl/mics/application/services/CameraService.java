package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera" + camera.getId());
        this.camera = camera;

    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " started");

        //Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {

            StampedDetectedObjects detectedObjectsAtTime = camera.handleTick(tick.getTime());

            if(camera.getStatus() == STATUS.UP && detectedObjectsAtTime != null && !detectedObjectsAtTime.getDetectedObjects().isEmpty()) {
                // Send event with detected objects
                sendEvent(new DetectObjectsEvent(getName(), detectedObjectsAtTime));
                System.out.println(getName() + " sent detected objects event");
            }

            //In case of a camera error
            else if (camera.getStatus()== STATUS.ERROR) {
                System.out.println(getName() + " crashed!");
                sendBroadcast(new CrashedBroadcast(getName(),camera));
                terminate();
            }

            //In case the camera shuts down
            else if (camera.getStatus()== STATUS.DOWN) {
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });


        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if(terminatedBroadcast.getSenderId().equals("Fusion Slam Service")) { //Terminate only if the fusionSlam send the broadcast
                System.out.println(getName() + " terminated by " + terminatedBroadcast.getSenderId());
                terminate();
            }
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            System.out.println(getName() + " crashed by " + crashedBroadcast.getSenderId());
            terminate();
        });
    }

}
