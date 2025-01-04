package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.example.messages.ExampleBroadcast;

import java.time.Duration;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    private int tickTimeInMillis;
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
        this.tickTimeInMillis = TickTime*1000;
        this.duration = Duration;
        currentTick = 1;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " started");

        // Handle Terminated Broadcast from Slam
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if(terminatedBroadcast.getSenderId().equals("Fusion Slam Service")) {
                System.out.println(getName() + " terminated by " + terminatedBroadcast.getSenderId());
                terminate();
            }
        });

        while(currentTick<=duration) { //While we haven't reached duration time, we will send TickBroadcast to all listeners.
            try {
                // Wait for TickTime between two Tick Broadcast
                Thread.sleep(tickTimeInMillis);
            } catch (InterruptedException e) {
                System.err.println("Thread was interrupted during sleep: " + e.getMessage());
                Thread.currentThread().interrupt(); //Restore the interrupted status
                break; //Exit the loop if the thread is interrupted
            }
            System.out.println(getName() + " sends tick broadcast");
            sendBroadcast(new TickBroadcast(getName(),currentTick));
            currentTick++;

            //Increase the SystemRunTime by 1
            StatisticalFolder.getInstance().incrementSystemRuntime();
        }

        //In case we reached the duration time
        sendBroadcast(new TerminatedBroadcast(getName()));
        terminate();
    }
}
