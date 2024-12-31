package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.LandMark;

import java.util.ArrayList;
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

        JsonOutputGenerator jsonOutputGenerator = new JsonOutputGenerator(landMarks);
        jsonOutputGenerator.set(22,13,13,7);
        jsonOutputGenerator.create();

    }
}
