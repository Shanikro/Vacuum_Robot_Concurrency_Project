package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;

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

        // Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            currentTick = tick.getTime();

            int detectedTime = currentTick - camera.getFrequency();
            if (detectedTime>0) {
                StampedDetectedObjects detectedObjects = camera.detectObjects(detectedTime);

                // Check if there is objects and the camera is on
                if (detectedObjects != null && camera.isUp()) {
                    // Send event with detected objects
                    Future<Boolean> futureObject = (Future<Boolean>) sendEvent(new DetectObjectsEvent(getName(),detectedTime, detectedObjects.getDetectedObjects()));

                    if(futureObject.get(tick.getDuration()-tick.getTime(), TimeUnit.MILLISECONDS)) {
                        StatisticalFolder.updateDetectedObjects(camera.getDetectedObjectsCount());
                    }
                }

                // Handle errors
                if (!camera.isUp()) {
                    sendBroadcast(new TerminatedBroadcast());
                    terminate();
                }
            }
        });

        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, t -> terminate());
    }

}
