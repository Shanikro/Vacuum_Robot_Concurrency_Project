package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private int currentTick;
    private GPSIMU location;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("Pose Service");
        this.location = gpsimu;
        this.currentTick = 0;

    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        System.out.println("Pose " + getName() + " started");

        // Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {

            currentTick = tick.getTime();
            Pose currentPose = null;
            for(Pose p : location.getPoseList()){
                if(p.getTime() == currentTick){
                    currentPose = p;
                    break;
                }
            }
            if(currentPose != null && location.getStatus()== STATUS.UP) {
                sendEvent(new PoseEvent(getName(), currentPose)); //TODO לבדוק אם צריך לשמור את הבוליאן שמתקבל
                System.out.println("gps" + getName() + "sent pose event");
            }

            // Handle errors
            if (!(location.getStatus() == STATUS.UP)) {
                System.out.println("Sender " + getName() + " stopped");
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }

        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
           terminate();
        });

    }
}
