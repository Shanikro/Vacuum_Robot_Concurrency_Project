package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

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
        });

        // Handle Detect Objects Event
        subscribeEvent(DetectObjectsEvent.class, detectObjectsevent ->{

            StampedDetectedObjects stampedDetectedObjects = detectObjectsevent.getDetectedObjects(); //Include list of detected objects


            for (DetectedObject detectedObject : stampedDetectedObjects.getDetectedObjects()){

                List<CloudPoint> listCoordinates = new LinkedList<>();//ליצור לפי הDB ליסט של קלאודפוינט לכל אוביקט TODO

                TrackedObject newTrackedObj = new TrackedObject(detectedObject.getId(),currentTick, detectedObject.getDescription(), listCoordinates);
                LiDar.addTrackedObject(newTrackedObj);
            }

            sendEvent(new TrackedObjectsEvent(getName(), LiDar.getLastTrackedObjects())); //TODO לבדוק אם צריך לשמור את הבוליאן שמתקבל
            System.out.println(getName() + "sent Tracked Objects event");

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
}
