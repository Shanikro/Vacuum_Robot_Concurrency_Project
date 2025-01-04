package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("Pose Service");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " started");

        // Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {

            Pose pose = gpsimu.handleTick(tick.getTime());

            if(gpsimu.getStatus() == STATUS.UP) {
                sendEvent(new PoseEvent(getName(), pose));
                System.out.println(getName() + " sent pose event");
            }

            //In case that no more data to read, finish
            if(gpsimu.getStatus() == STATUS.DOWN){
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }

        });

        // Handle Terminated Broadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if(terminatedBroadcast.getSenderId().equals("Fusion Slam Service")) {
                System.out.println(getName() + " terminated by " + terminatedBroadcast.getSenderId());
                terminate();
            }
        });

        // Handle Crashed Broadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            System.out.println(getName() + " crashed by " + crashedBroadcast.getSenderId());
            terminate();
        });

    }
}
