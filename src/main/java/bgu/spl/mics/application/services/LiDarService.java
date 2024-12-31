package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.LinkedList;
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
    private int currentTick;
    private int stampedPointsUntilFinish;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("lidarWorker" + LiDarWorkerTracker.getId());
        this.LiDar = LiDarWorkerTracker;
        this.currentTick = 0;
        this.stampedPointsUntilFinish = LiDarDataBase.getInstance().getCloudPoints().size();
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
            currentTick = tick.getTime();

            List<TrackedObject> trackedObjectsToSlam = new LinkedList<>();
            for(TrackedObject o : LiDar.getLastTrackedObjects()){ //TODO סנכרון
                if(o.getTime() == currentTick - LiDar.getFrequency()) {
                    trackedObjectsToSlam.add(o); //Add this object to the list used by Fusion Slam
                    LiDar.getLastTrackedObjects().remove(o); //Remove the object from the last tracked object list of the lidar
                }
            }



            if (!trackedObjectsToSlam.isEmpty()) { //Send the tracked objects

                //Update the count until the LiDar finish
                stampedPointsUntilFinish = stampedPointsUntilFinish - trackedObjectsToSlam.size();

                //Update the number of Tracked Objects in the Statistical Folder
                StatisticalFolder.getInstance().addTrackedObjects(trackedObjectsToSlam.size());

                sendEvent(new TrackedObjectsEvent(getName(), trackedObjectsToSlam)); //TODO לבדוק אם צריך לשמור את הבוליאן שמתקבל
                System.out.println(getName() + "sent Tracked Objects event");
            }

            //In case that the LiDar finish
            if(stampedPointsUntilFinish == 0 && LiDar.getLastTrackedObjects().isEmpty()){
                LiDar.setStatus(STATUS.DOWN);
                System.out.println("Sender " + getName() + " terminated!");
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }

        });

        // Handle Detect Objects Event
        subscribeEvent(DetectObjectsEvent.class, detectObjectsevent ->{

            StampedDetectedObjects stampedDetectedObjects = detectObjectsevent.getDetectedObjects(); //Include list of detected objects
            int trackedTime = stampedDetectedObjects.getTime() + LiDar.getFrequency(); //Time to send as event

            List<TrackedObject> trackedObjectsToSlam = new LinkedList<>();
           //TODO סנכרון
            for (DetectedObject detectedObject : stampedDetectedObjects.getDetectedObjects()){

                List<CloudPoint> listCoordinates = getCloudPointList(detectedObject,stampedDetectedObjects.getTime()); //Create list of coordinates (cloud point) for the corresponding Object

                //In case of LiDar error,break
                if (LiDar.getStatus() == STATUS.ERROR) {
                    break;
                }

                //If not error, create corresponding Tracked Object
                TrackedObject newTrackedObj = new TrackedObject(detectedObject.getId(),trackedTime, detectedObject.getDescription(), listCoordinates);

                if(trackedTime<=currentTick){
                    trackedObjectsToSlam.add(newTrackedObj); //The time Tick already passed, we can send it
                }
                else {
                    LiDar.addTrackedObject(newTrackedObj); //Add the new tracked object to the LiDAR's list, waiting for the appropriate time to send it.
                }
            }

            //In case of LiDar error
            if (LiDar.getStatus() == STATUS.ERROR) {
                System.out.println("Sender " + getName() + " crashed!");
                sendBroadcast(new CrashedBroadcast(getName()));
                terminate();
            }

            //If the LiDar is UP, Send the tracked objects whose time has already passed
            if (LiDar.getStatus() == STATUS.UP && !trackedObjectsToSlam.isEmpty()){

                //Update the count until the LiDar finish
                stampedPointsUntilFinish = stampedPointsUntilFinish - trackedObjectsToSlam.size();

                //Update the number of Tracked Objects in the Statistical Folder
                StatisticalFolder.getInstance().addTrackedObjects(trackedObjectsToSlam.size());

                sendEvent(new TrackedObjectsEvent(getName(), trackedObjectsToSlam)); //TODO לבדוק אם צריך לשמור את הבוליאן שמתקבל
                System.out.println(getName() + "sent Tracked Objects event");
            }

            //In case that the LiDar finish
            if(stampedPointsUntilFinish == 0 && LiDar.getLastTrackedObjects().isEmpty()){
                LiDar.setStatus(STATUS.DOWN);
                System.out.println("Sender " + getName() + " terminated!");
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }

        });

        // Handle Terminated Broadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println("LiDar " + LiDar.getId() + " terminated by" + terminatedBroadcast.getSenderId());
            terminate();
        });

        // Handle Crashed Broadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->{
            System.out.println("LiDar " + LiDar.getId() + " crashed by " + crashedBroadcast.getSenderId());
            terminate();
        });

    }

    private List<CloudPoint> getCloudPointList(DetectedObject detectedObject, int detectedTime) {

        LinkedList<CloudPoint> output = new LinkedList<>();

        List<StampedCloudPoints> dataBase = LiDarDataBase.getInstance().getCloudPoints();

        for (StampedCloudPoints s : dataBase) { //Find the corresponding StampedCloudPoints According to detectedTime + frequency

            //Check ERROR id's
            if (s.getId().equals("ERROR")) {
                LiDar.setStatus(STATUS.ERROR);
                break;
                //TODO: להוסיף לג'ייסון כוול התיאור
            }

            //If everything OK
            else if (s.getTime() == detectedTime + LiDar.getFrequency() && s.getId().equals(detectedObject.getId())) {
                for (List<Double> l : s.getCloudPoints()) { //Copy each list with x and y to a CloudPoint object
                    CloudPoint newPoint = new CloudPoint(l.get(0), l.get(1));
                    output.add(newPoint);
                    break;
                }
            }
        }
        return output;
    }

}
