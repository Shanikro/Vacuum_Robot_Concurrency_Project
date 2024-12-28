package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;

import java.util.LinkedList;
import java.util.List;

import static java.lang.reflect.Array.get;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private FusionSlam fusionSlam;
    private int currentTick;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("Change_This_Name");
        this.fusionSlam = fusionSlam;
        this.currentTick = 0;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        //check what it needs to do in every broadcast/event
        subscribeBroadcast(TickBroadcast.class, tick -> {
            currentTick = tick.getTime();
        });

        subscribeEvent(TrackedObjectsEvent.class, event ->{
           fusionSlam.getTrackedObjects().add(event);
            //fine the time of the event
            int time = event.getTrackedObjects().get(0).getTime();
           //search for the corresponding pose
            Pose pose = fusionSlam.getPoseByTime(time);
            //if it finds he uploads the map
            if(pose != null)
                fusionSlam.calculate(event , pose);

        });

        subscribeEvent(PoseEvent.class, event ->{
            fusionSlam.addPose(event.getPose());
            //poses.add(event);
            //search for the corresponding object
            TrackedObjectsEvent trackedObjectsEvent = fusionSlam.getMatchingEvent(event.getPose().getTime());
            //if it finds he uploads the map
            if(trackedObjectsEvent != null)
                fusionSlam.calculate(trackedObjectsEvent, event.getPose());

        });

        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> terminate());

        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{

        });
    }
}
