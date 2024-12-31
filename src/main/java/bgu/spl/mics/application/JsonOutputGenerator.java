package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.StatisticalFolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class JsonOutputGenerator {

    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private List<LandMark> landMarks;

    public JsonOutputGenerator(List<LandMark> list){
        landMarks = list;
        systemRuntime = StatisticalFolder.getInstance().getSystemRuntime();
        numDetectedObjects = StatisticalFolder.getInstance().getNumDetectedObjects();
        numTrackedObjects = StatisticalFolder.getInstance().getNumTrackedObjects();
        numLandmarks = StatisticalFolder.getInstance().getNumLandmarks();
    }


    public void create() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("output_file.json")) {
            // Serialize Java objects to JSON file
            gson.toJson(this, writer);
            System.out.println("FusionSlam data has been written to output_file.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void set(int i, int i1, int i2, int i3) { //TODO:למחוק כשנסיים
        systemRuntime = i;
        numDetectedObjects = i1;
        numTrackedObjects = i2;
        numLandmarks = i3;
    }
}
