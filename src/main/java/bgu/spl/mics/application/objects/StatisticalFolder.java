package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;
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

    private List<Camera> cameraList;
    private List<LiDarWorkerTracker> lidarList;
    private AtomicInteger sensorsInAction; //When equals 0, the FusionSlam should terminate

    public StatisticalFolder(){

        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);

        this.sensorsInAction = new AtomicInteger(0);
        this.cameraList = new LinkedList<>();
        this.lidarList = new LinkedList<>();

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
    public int getSensorsInAction() {
        return sensorsInAction.get();
    }
    public List<Camera> getCameraList() {
        return cameraList;
    }
    public List<LiDarWorkerTracker> getLiDarList() {
        return lidarList;
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
    public void incrementSensorsInAction(){
        sensorsInAction.incrementAndGet();
    }
    public void decrementSensorsInAction(){
        sensorsInAction.decrementAndGet();
    }

    public void addCamera(Camera camera) {
        cameraList.add(camera);
    }

    public void addLiDar(LiDarWorkerTracker liDarWorkerTracker) {
        lidarList.add(liDarWorkerTracker);
    }
}
