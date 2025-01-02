package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DemoMain {
    public static void main(String[] args) {

        List<LandMark> landMarks = new ArrayList<>();
        // יצירת אובייקט LandMark לדוגמה - Wall_1
        List<CloudPoint> wall1Coordinates = new ArrayList<>();
        wall1Coordinates.add(new CloudPoint(1.1887387639977982, 5.046603301251042));
        wall1Coordinates.add(new CloudPoint(1.2533775541582042, 5.113604111414717));
        LandMark wall1 = new LandMark("Wall_1", "Wall", wall1Coordinates);

        // יצירת אובייקט LandMark לדוגמה - Door
        List<CloudPoint> doorCoordinates = new ArrayList<>();
        doorCoordinates.add(new CloudPoint(-2.913332578606659, -1.1554635639732926));
        doorCoordinates.add(new CloudPoint(-2.7427859966862367, -1.4731329886827864));
        LandMark door = new LandMark("Door", "Door", doorCoordinates);

        // הוספת הלנדמרקס לרשימה
        landMarks.add(wall1);
        landMarks.add(door);

        List<Pose> poses = new ArrayList<>();
        poses.add(new Pose(1, 1,3,1));
        poses.add(new Pose(2, 3,3,4));

        List<DetectedObject> dlist = new LinkedList<>();
        DetectedObject o = new DetectedObject("Error", "Camera disconnected");
        DetectedObject o1 = new DetectedObject("wall2", "wall");
        dlist.add(o1);
        List<StampedDetectedObjects> list = new LinkedList<>();
        StampedDetectedObjects sd = new StampedDetectedObjects(1,dlist);
        list.add(sd);
        Camera c = new Camera(1,2,list);

        //c.error = o.getDescription();
        //c.lastStampedDetectedObject = sd;

        LiDarWorkerTracker l = new LiDarWorkerTracker(1,2);
        List<CloudPoint> ll = new LinkedList<>();
        ll.add(new CloudPoint(3.1,4.2));
        ll.add(new CloudPoint(8,92));
        //l.setLastTrackedObject() = new TrackedObject("wall1",2,"wall",ll);

        JsonOutputErrorGenerator outputData = new JsonOutputErrorGenerator(l,poses,new StatisticalFolderAndLandmarks(StatisticalFolder.getInstance(),landMarks));
        outputData.create();

    }
}
