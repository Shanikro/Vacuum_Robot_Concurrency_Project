package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;
    private int currentTick;
    private List<StampedDetectedObjects> list;


    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera" + camera.getId());
        this.camera = camera;
        this.currentTick = 0;
        this.list = new ArrayList<>();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println("Camera " + getName() + " started");
        // Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            currentTick = tick.getTime();
            list.add(camera.detectObjects(currentTick)); // קודם מוסיפים לשדה את מה שהתגלה בטיק הנוכחי

            //int detectedTime = currentTick - camera.getFrequency();
            //if(detectedTime == currentTick + camera.getFrequency()){
            //if (detectedTime>0) {
            // ואז עוברים על השדה(שמחזיק את כל מה שהתגלה עד הטיק הזה) ומה שבזמן של הטיק המתאים היא שמה ברשימה חדשה שאתה הרשימה הזאת נשלח באיוונט
            List<DetectedObject> detectedObjects = new LinkedList<>();// = camera.detectObjects(detectedTime);
            for(StampedDetectedObjects s : list){
                if(s.getTime() == currentTick + camera.getFrequency()) {
                    detectedObjects = s.getDetectedObjects();
                    break;
                }
            }
            // Check if there is objects and the camera is on
            if (detectedObjects != null && camera.isUp()) {
                // Send event with detected objects                                                        החלפתי את detectedtime
                Future<Boolean> futureObject = (Future<Boolean>) sendEvent(new DetectObjectsEvent(getName(),currentTick, detectedObjects));//.getDetectedObjects()));
                System.out.println("Camera" + getName() + " send detected objects event");
                //updates in the object - לפי מה שלוטם אמר העדכון אמור להיות באובייקט עצמו
//                    if(futureObject.get(tick.getDuration()-tick.getTime(), TimeUnit.MILLISECONDS)) {
//                        StatisticalFolder.updateDetectedObjects(camera.getDetectedObjectsCount());
//                    }
            }

                // Handle errors
                if (!camera.isUp()) {
                    System.out.println("Sender " + getName() + " stopped");
                    sendBroadcast(new TerminatedBroadcast(""+camera.getId()));
                    terminate();
                }
            //}
        });

        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, t -> terminate());
    }

}
