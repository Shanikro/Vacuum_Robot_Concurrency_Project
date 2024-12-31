package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

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

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("lidarWorker" + LiDarWorkerTracker.getId());
        this.LiDar = LiDarWorkerTracker;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        System.out.println("LiDar " + getName() + " started");

        //Notify FusionSlam that new object registered
        sendEvent(new RegisterEvent(getName()));
        System.out.println(getName() + "sent Register event");

        // Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            List<TrackedObject> trackedObjectsToSlam = LiDar.handleTick(tick.getTime());
            //In case the LiDAR is UP or DOWN
            sendEventByStatus(trackedObjectsToSlam);
        });

        // Handle Detect Objects Event
        subscribeEvent(DetectObjectsEvent.class, detectObjectsevent ->{

            List<TrackedObject> trackedObjectsToSlam = LiDar.handleDetectObjects(detectObjectsevent);

            //In case of LiDar error
            if (LiDar.getStatus() == STATUS.ERROR) {
                System.out.println("Sender " + getName() + " crashed!");
                sendBroadcast(new CrashedBroadcast(getName()));
                terminate();
            }

            //In case the LiDAR is UP or DOWN
            sendEventByStatus(trackedObjectsToSlam);

        });

        // Handle Terminated Broadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if(terminatedBroadcast.getSenderId().equals("Fusion Slam Service")) {
                System.out.println("LiDar " + LiDar.getId() + " terminated by " + terminatedBroadcast.getSenderId());
                terminate();
            }
        });

        // Handle Crashed Broadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            System.out.println("LiDar " + LiDar.getId() + " crashed by " + crashedBroadcast.getSenderId());
            terminate();
        });

    }

    private void sendEventByStatus(List<TrackedObject> trackedObjectsToSlam) {

        if(LiDar.getStatus() == STATUS.UP) {
            // Send event with detected objects
            sendEvent(new TrackedObjectsEvent(getName(), trackedObjectsToSlam)); //TODO לבדוק אם צריך לשמור את הבוליאן שמתקבל
            System.out.println(getName() + "sent Tracked Objects event");
        }

        //In case the camera shuts down
        else if (LiDar.getStatus()== STATUS.DOWN) {
            System.out.println("Sender " + getName() + " terminated!");
            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        }
    }

}
