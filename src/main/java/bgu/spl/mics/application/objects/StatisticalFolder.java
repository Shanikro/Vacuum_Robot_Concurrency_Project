package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system (in ticks),
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    private static final StatisticalFolder instance = new StatisticalFolder();

    private AtomicInteger systemRuntime;
    private AtomicInteger numDetectedObjects;
    private AtomicInteger numTrackedObjects;
    private AtomicInteger numLandmarks;

    public StatisticalFolder(){

        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);

    }

    //Static method to get the Singleton instance
    public static StatisticalFolder getInstance() {
        return instance;
    }

    //Getters
    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    //Setters
    public void incrementSystemRuntime(){
        systemRuntime.incrementAndGet();
    }
    public void addTrackedObjects(int amount){
        numTrackedObjects.addAndGet(amount);
    }
    public void addDetectedObjects(int amount){
        numDetectedObjects.addAndGet(amount);
    }
    public void incrementLandMarks(){
        numLandmarks.incrementAndGet();
    }
}
