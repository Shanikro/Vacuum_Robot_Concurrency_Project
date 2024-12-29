package bgu.spl.mics.application.services;

import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<Integer, List<TrackedObject>> pendingTrackedObjects; //A data structure that temporarily stores objects whose corresponding Pose not arrived yet.
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("Fusion Slam Service");
        this.fusionSlam = fusionSlam;
        this.currentTick = 0;
        this.pendingTrackedObjects = new ConcurrentHashMap<>();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {

        //Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            currentTick = tick.getTime();
        });

        //Handle TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent ->{
            int time = trackedObjectsEvent.getTrackedObjects().get(0).getTime(); //Check the time that tracked

            //In case the corresponding Pose has not appeared yet.
            if (time > currentTick){ //TODO לבדוק אם זה אומר שבוודאות הגיע כבר הפוס
                pendingTrackedObjects.put(time, trackedObjectsEvent.getTrackedObjects()); //Save the objects for later
            }

            else{
                Pose correspondingPose = fusionSlam.getPoseByTime(time);
                for (TrackedObject object : trackedObjectsEvent.getTrackedObjects()) {
                    fusionSlam.calculate(object, correspondingPose);
                }
            }

        });

        //Handle PoseEvent
        subscribeEvent(PoseEvent.class, pose -> {
            int time = pose.getPose().getTime(); //Pose time
            fusionSlam.addPose(pose.getPose()); //Add Pose to the pose list of FusionSlam

            //Check if there is TrackedObjects that waiting for the pose
            if (pendingTrackedObjects.containsKey(time)) {
                List<TrackedObject> matchedTrackedObjects = pendingTrackedObjects.remove(time); //Remove them
                for (TrackedObject object : matchedTrackedObjects) { //TODO Calculate the global pose LandMarkלבדוק איפה האחראיות להוסיף ל
                    fusionSlam.calculate(object, pose.getPose());
                }
            }
        });

        //Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            terminate();
        });

        //Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            terminate();
        });
    }
}
