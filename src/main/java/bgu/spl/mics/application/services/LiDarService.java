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

            //TODO: need to understand how to take the coordinates from the database
            StampedDetectedObjects s = detectObjectsevent.getDetectedObjects();

            for(DetectedObject a : s.getDetectedObjects()){
                trackedObjects.add( new TrackedObject(a.getId(),currentTick, a.getDescription()));
                //TODO: i think we need to add to the constructor also coordinates and then take them from the database
            }
            List<TrackedObject> currentTracked = new LinkedList<>();
            for(TrackedObject t : trackedObjects){
                if(t.getTime() == currentTick +LiDar.getFrequency())
                    currentTracked.add(t);
            }

            if(!(trackedObjects.isEmpty()) && LiDar.getStatus()== STATUS.UP) {
                Future<Boolean> futureObject = sendEvent(new TrackedObjectsEvent(getName(), currentTracked));
                System.out.println("LiDar" +LiDar.getId() + "sent traked object event");
            }

            // Handle errors
            if (!(LiDar.getStatus() == STATUS.UP)) {
                System.out.println("Sender " + getName() + " stopped");
                sendBroadcast(new TerminatedBroadcast(""+LiDar.getId()));
                terminate();
            }

        });

        // Handle Terminated Broadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            LiDar.setStatus(STATUS.DOWN);
            System.out.println("Lidar " + LiDar.getId() + " stopped");
            terminate();
        });

        // Handle Crashed Broadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            terminate();
        });

    }
}
