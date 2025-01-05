import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LiDarTest {

    private LiDarWorkerTracker lidarWorker;
    private List<StampedCloudPoints> cloudPoints;

    @BeforeEach
    public void setUp() {

        // Reset the data in LiDarDataBase
        LiDarDataBase.getInstance().getCloudPoints().clear();

        // Initialize mock data for LiDarDataBase
        cloudPoints = new LinkedList<>();
        cloudPoints.add(new StampedCloudPoints( "Wall_1",1, Arrays.asList(Arrays.asList(1.0, 2.0), Arrays.asList(2.0, 3.0))));
        cloudPoints.add(new StampedCloudPoints( "Door_1",2, Arrays.asList(Arrays.asList(3.0, 4.0))));
        LiDarDataBase.getInstance().getCloudPoints().addAll(cloudPoints);

        // Initialize LiDarWorkerTracker
        lidarWorker = new LiDarWorkerTracker(1, 1);
    }

    @Test
    public void testHandleTick_ValidTracking() {
        TrackedObject trackedObject1 = new TrackedObject("Wall_1", 1, "Wall", Arrays.asList(new CloudPoint(1.0, 2.0)));
        TrackedObject trackedObject2 = new TrackedObject("Door_1", 2, "Door", Arrays.asList(new CloudPoint(3.0, 4.0)));
        lidarWorker.addTrackedObject(trackedObject1);
        lidarWorker.addTrackedObject(trackedObject2);

        List<TrackedObject> result = lidarWorker.handleTick(3);

        assertEquals(1, result.size(), "Should track 1 object at tick 2");
        assertEquals("Door_1", result.get(0).getId(), "Tracked object ID should match");
        assertEquals(STATUS.UP, lidarWorker.getStatus(), "LiDar status should remain UP");
    }

    @Test
    public void testHandleTick_LiDarDown() {
        TrackedObject trackedObject = new TrackedObject("Wall_1", 1, "Wall", Arrays.asList(new CloudPoint(1.0, 2.0)));
        TrackedObject trackedObject2 = new TrackedObject("Door_1", 2, "Door", Arrays.asList(new CloudPoint(3.0, 4.0)));

        lidarWorker.addTrackedObject(trackedObject);
        lidarWorker.addTrackedObject(trackedObject2);

        lidarWorker.handleTick(1);
        lidarWorker.handleTick(2);
        lidarWorker.handleTick(3);
        lidarWorker.handleTick(4);


        assertEquals(STATUS.DOWN, lidarWorker.getStatus(), "LiDar status should be DOWN after finishing all objects");
    }

    @Test
    public void testHandleDetectObjects_ValidDetection() {
        StampedDetectedObjects stampedObjects = new StampedDetectedObjects(1, Arrays.asList(new DetectedObject("Wall_1", "Wall")));
        DetectObjectsEvent event = new DetectObjectsEvent("camera",stampedObjects);

        lidarWorker.handleDetectObjects(event);
        List<TrackedObject> result = lidarWorker.getLastTrackedObjects();

        assertEquals(1, result.size(), "Should detect 1 object");
        assertEquals("Wall_1", result.get(0).getId(), "Detected object ID should match");
        assertEquals(STATUS.UP, lidarWorker.getStatus(), "LiDar status should remain UP");
    }

    @Test
    public void testHandleDetectObjects_LiDarError() {
        StampedDetectedObjects stampedObjects = new StampedDetectedObjects(0, Arrays.asList(new DetectedObject("ERROR", "Fault")));
        DetectObjectsEvent event = new DetectObjectsEvent("camera",stampedObjects);
        LiDarDataBase.getInstance().getCloudPoints().add(new StampedCloudPoints("ERROR", 0,Arrays.asList(Arrays.asList(3.0, 4.0))));

        List<TrackedObject> result = lidarWorker.handleDetectObjects(event);

        assertTrue(result.isEmpty(), "No objects should be tracked in case of ERROR");
        assertEquals(STATUS.ERROR, lidarWorker.getStatus(), "LiDar status should be ERROR");
    }
}
