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
    private int currentTick;
    private int stampedObjectUntilFinish;


    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera" + camera.getId());
        this.camera = camera;
        this.currentTick = 0;
        this.stampedObjectUntilFinish = camera.getDetectedObjectsList().size();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println("Camera " + getName() + " started");

        //Notify FusionSlam that new object registered
        sendEvent(new RegisterEvent(getName()));
        System.out.println(getName() + "sent Register event");

        //Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {

            currentTick = tick.getTime();

            StampedDetectedObjects detectedObjectsAtTime = null;
            for(StampedDetectedObjects s : camera.getDetectedObjectsList()){
                if(s.getTime() == currentTick - camera.getFrequency()) {
                    detectedObjectsAtTime = s;
                    break;
                }
            }

            // Check if there is objects
            if (detectedObjectsAtTime != null) {

                //Check ERROR id's
                for (DetectedObject o : detectedObjectsAtTime.getDetectedObjects()) {
                    if (o.getId().equals("ERROR")) {
                        camera.setStatus(STATUS.ERROR);
                        //TODO: להוסיף לג'ייסון כוול התיאור
                    }
                    break;
                }

                //If everything OK
                if (camera.getStatus() == STATUS.UP) {
                    stampedObjectUntilFinish--; //Update the count until the camera finish

                    //Update the number of Detected Objects in the Statistical Folder
                    StatisticalFolder.getInstance().addDetectedObjects(detectedObjectsAtTime.getDetectedObjects().size());

                    // Send event with detected objects
                    Future<Boolean> futureObject = (Future<Boolean>) sendEvent(new DetectObjectsEvent(getName(), detectedObjectsAtTime));
                    System.out.println("Camera" + getName() + " send detected objects event");  //TODO: check what we need to do with the future
                }
            }

            //In case of camera error
            if (camera.getStatus()== STATUS.ERROR) {
                System.out.println("Sender " + getName() + " crashed!");
                sendBroadcast(new CrashedBroadcast(getName()));
                terminate();
            }

            //In case that the camera finish
            if(stampedObjectUntilFinish == 0){
                camera.setStatus(STATUS.DOWN);
                System.out.println("Sender " + getName() + " terminated!");
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });


        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println("Camera " + camera.getId() + " terminated by" + terminatedBroadcast.getSenderId());
            terminate();
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            System.out.println("Camera " + camera.getId() + " crashed by " + crashedBroadcast.getSenderId());
            terminate();
        });
    }

}
