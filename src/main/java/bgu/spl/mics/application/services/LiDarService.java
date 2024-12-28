package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.TrackedObject;

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
    private List<TrackedObject> trackedObjects;
    private int currentTick;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("lidarWorker" + LiDarWorkerTracker.getId());
        this.LiDar = LiDarWorkerTracker;
        this.trackedObjects = new LinkedList<>();
        this.currentTick = 0;
        // TODO Implement this
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
            for(TrackedObject t : LiDar.getLastTrackedObjects()) {
                if(t.getTime() == currentTick)//not sure if this is the term
                    trackedObjects.add(t);
            }
            if(trackedObjects != null && LiDar.getStatus()== STATUS.UP) {
                Future<Boolean> futureObject = sendEvent(new TrackedObjectsEvent(getName(), trackedObjects));
                System.out.println("LiDar" + "sent traked object event");
            }

            // Handle errors
            if (!(LiDar.getStatus() == STATUS.UP)) {
                System.out.println("Sender " + getName() + " stopped");
                sendBroadcast(new TerminatedBroadcast(""+LiDar.getId()));
                terminate();
            }

        });

        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, t -> terminate());
    }
}
