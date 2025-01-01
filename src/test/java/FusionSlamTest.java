import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.getLandMarks().clear(); // Clear landmarks before each test
    }

    @Test
    public void testUpdateMap_NewLandmark() {
        Pose pose = new Pose(0.0f, 0.0f, 0.0f, 1);
        TrackedObject trackedObject = new TrackedObject("Wall_1",1, "Wall", Arrays.asList(new CloudPoint(1.0, 2.0)));

        fusionSlam.updateMap(trackedObject, pose);

        List<LandMark> landmarks = fusionSlam.getLandMarks();
        assertEquals(1, landmarks.size(), "Landmarks size should be 1");
        assertEquals("Wall_1", landmarks.get(0).getId(), "Landmark ID should match");
        assertEquals(1.0, landmarks.get(0).getCoordinates().get(0).getX(), 0.001);
        assertEquals(2.0, landmarks.get(0).getCoordinates().get(0).getY(), 0.001);
    }

    @Test
    public void testUpdateMap_ExistingLandmark() {

        Pose pose1 = new Pose(0.0f, 0.0f, 0.0f, 1);
        Pose pose2 = new Pose(1.0f, 1.0f, 45.0f, 2);

        TrackedObject trackedObject1 = new TrackedObject("Wall_1", 1, "Wall",
                Arrays.asList(new CloudPoint(1.0, 2.0)));
        TrackedObject trackedObject2 = new TrackedObject("Wall_1", 2, "Wall",
                Arrays.asList(new CloudPoint(2.0, 3.0)));

        fusionSlam.updateMap(trackedObject1, pose1);
        fusionSlam.updateMap(trackedObject2, pose2);

        List<LandMark> landmarks = fusionSlam.getLandMarks();
        assertEquals(1, landmarks.size(), "Landmarks size should still be 1");

        assertEquals("Wall_1", landmarks.get(0).getId(), "Landmark ID should match");

        List<CloudPoint> points = landmarks.get(0).getCoordinates();

        assertEquals(1, points.size(), "Coordinates should be merged into a single point");

        assertEquals(0.6465, points.get(0).getX(), 0.001, "X coordinate should be averaged");
        assertEquals(3.2678, points.get(0).getY(), 0.001, "Y coordinate should be averaged");
    }


    @Test
    public void testUpdateMap_NoPoseUpdate() {
        Pose pose = new Pose(0.0f, 0.0f, 90.0f, 1);
        TrackedObject trackedObject = new TrackedObject("Door_1",1 ,"Door", Arrays.asList(new CloudPoint(0.0, 1.0)));

        fusionSlam.updateMap(trackedObject, pose);

        List<LandMark> landmarks = fusionSlam.getLandMarks();
        assertEquals(1, landmarks.size(), "Landmarks size should be 1");
        assertEquals("Door_1", landmarks.get(0).getId(), "Landmark ID should match");

        List<CloudPoint> points = landmarks.get(0).getCoordinates();
        assertEquals(1, points.size(), "Coordinates size should be 1");
        assertEquals(-1.0, points.get(0).getX(), 0.001);
        assertEquals(0.0, points.get(0).getY(), 0.001);
    }
}
