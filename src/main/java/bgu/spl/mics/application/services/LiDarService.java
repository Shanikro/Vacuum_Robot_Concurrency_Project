package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    private LiDarWorkerTracker LiDar;
    private int currentTick;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("lidarWorker" + LiDarWorkerTracker.getId());
        this.LiDar = LiDarWorkerTracker;
        this.currentTick = 0;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        System.out.println("LiDar " + getName() + " started");

        // Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            currentTick = tick.getTime();

            List<TrackedObject> trackedObjectsToSlam = new LinkedList<>();
            for(TrackedObject o : LiDar.getLastTrackedObjects()){
                if(o.getTime() == currentTick - LiDar.getFrequency()) {
                    trackedObjectsToSlam.add(o); //Add this object to the list used by Fusion Slam
                    LiDar.getLastTrackedObjects().remove(o); //Remove the object from the last tracked object list of the lidar
                }
            }

            sendEvent(new TrackedObjectsEvent(getName(), trackedObjectsToSlam)); //TODO לבדוק אם צריך לשמור את הבוליאן שמתקבל
            System.out.println(getName() + "sent Tracked Objects event");

        });

        // Handle Detect Objects Event
        subscribeEvent(DetectObjectsEvent.class, detectObjectsevent ->{

            StampedDetectedObjects stampedDetectedObjects = detectObjectsevent.getDetectedObjects(); //Include list of detected objects

            for (DetectedObject detectedObject : stampedDetectedObjects.getDetectedObjects()){
                List<CloudPoint> listCoordinates = getCloudPointList(detectedObject); //Create list of coordinates (cloud point) for the corresponding Object
                //Create corresponding Tracked Object
                TrackedObject newTrackedObj = new TrackedObject(detectedObject.getId(),currentTick, detectedObject.getDescription(), listCoordinates);
                LiDar.addTrackedObject(newTrackedObj); //Add the new tracked object to the LiDAR's list, waiting for the appropriate time to send it.
            }

        });

        // Handle Terminated Broadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println("Lidar " + LiDar.getId() + " stopped");
            terminate();
        });

        // Handle Crashed Broadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            terminate();
        });

    }

    private List<CloudPoint> getCloudPointList(DetectedObject detectedObject) {
        //TODO: ליצור מהדאטהבייסלידר . לעבור על כל הרשימה של StampedCloudPoints
        // ולבדוק התאמה לפי זמן ואיידי

        return new ArrayList<>();
    }
}
