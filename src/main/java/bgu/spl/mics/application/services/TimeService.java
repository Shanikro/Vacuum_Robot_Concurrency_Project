package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.example.messages.ExampleBroadcast;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    private int tickTime;
    private int duration;
    private int currentTick;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in seconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("Time Service");
        this.tickTime = TickTime;
        this.duration = Duration;
        currentTick = 1;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        System.out.println("Sender " + getName() + " started");
        while(currentTick<=duration) { //While we haven't reached duration time, we will send TickBroadcast to all listeners.
            sendBroadcast(new TickBroadcast(getName(),currentTick));
            currentTick++;
            StatisticalFolder.getInstance().incrementSystemRuntime(); //Increase the SystemRunTime by 1
        }

        //In case we reached the duration time
        sendBroadcast(new TerminatedBroadcast(getName()));
        terminate();
    }
}
