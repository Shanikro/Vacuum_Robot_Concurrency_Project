import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class CameraTest {

    private Camera camera;
    private List<StampedDetectedObjects> detectedObjectsList;

    @BeforeEach
    public void setUp() {
        detectedObjectsList = new LinkedList<>();
        detectedObjectsList.add(new StampedDetectedObjects(1, Arrays.asList(new DetectedObject("Wall_1", "Wall"))));
        detectedObjectsList.add(new StampedDetectedObjects(2, Arrays.asList(new DetectedObject("Door_1", "Door"))));
        camera = new Camera(1, 1, detectedObjectsList);
    }

    @Test
    public void testHandleTick_ValidDetection() {
        StampedDetectedObjects result = camera.handleTick(2);

        assertNotNull(result, "Detected objects should not be null");
        assertEquals(1, result.getTime(), "Time should match the tick");
        assertEquals(1, result.getDetectedObjects().size(), "Should detect 1 object");
        assertEquals("Wall_1", result.getDetectedObjects().get(0).getId(), "Object ID should match");
        assertEquals(STATUS.UP, camera.getStatus(), "Camera status should remain UP");
    }

    @Test
    public void testHandleTick_ErrorDetection() {
        detectedObjectsList.add(new StampedDetectedObjects(3, Arrays.asList(new DetectedObject("ERROR", "Camera Disconnected"))));
        camera = new Camera(1, 1, detectedObjectsList);

        camera.handleTick(4);

        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera status should be ERROR");
    }

    @Test
    public void testHandleTick_NoDetection() {
        StampedDetectedObjects result = camera.handleTick(4);

        assertNull(result, "No detection should return null");
        assertEquals(STATUS.UP, camera.getStatus(), "Camera status should remain UP");
    }

    @Test
    public void testHandleTick_CameraDown() {
        camera.handleTick(1);
        camera.handleTick(2);
        camera.handleTick(3);
        camera.handleTick(4);

        assertEquals(STATUS.DOWN, camera.getStatus(), "Camera status should be DOWN after processing all objects");
    }
}
