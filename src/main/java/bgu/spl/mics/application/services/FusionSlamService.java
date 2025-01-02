package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private FusionSlam fusionSlam;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("Fusion Slam Service");
        this.fusionSlam = fusionSlam;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {

        //Handle RegisterEvent
        subscribeEvent(RegisterEvent.class, event ->{
            fusionSlam.handleRegister();
        });

        //Handle TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent ->{
            fusionSlam.handleTrackedObjects(trackedObjectsEvent);
        });

        //Handle PoseEvent
        subscribeEvent(PoseEvent.class, poseEvent -> {
            fusionSlam.handlePose(poseEvent);
        });

        //Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {

            if(terminatedBroadcast.getSenderId().equals("Time Service")){ //If the duration has passed, finish
                System.out.println(getName() + " terminated by" + terminatedBroadcast.getSenderId());
                sendBroadcast(new TerminatedBroadcast(getName()));
                fusionSlam.makeOutputJson();
                terminate();
            }

            else {
                fusionSlam.handleTerminate();
                if(fusionSlam.getSensorsInAction() == 0){ //If all the objects have no more data , finish
                    System.out.println(getName() + " terminated!");
                    fusionSlam.makeOutputJson();
                    terminate();
                }
            }
        });

        //Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            System.out.println(getName() + " crashed by " + crashedBroadcast.getSenderId());
            fusionSlam.makeOutputErrorJson(crashedBroadcast.getObject());
            terminate();
        });
    }
}
