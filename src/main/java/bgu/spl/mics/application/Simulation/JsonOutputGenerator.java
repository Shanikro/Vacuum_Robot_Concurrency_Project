package bgu.spl.mics.application.Simulation;

import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.StatisticalFolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonOutputGenerator {

    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private Map<String, LandMark> landMarks;

    public JsonOutputGenerator(List<LandMark> list){
        systemRuntime = StatisticalFolder.getInstance().getSystemRuntime();
        numDetectedObjects = StatisticalFolder.getInstance().getNumDetectedObjects();
        numTrackedObjects = StatisticalFolder.getInstance().getNumTrackedObjects();
        numLandmarks = StatisticalFolder.getInstance().getNumLandmarks();
        landMarks = makeAsMap(list);
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

    private Map<String, LandMark> makeAsMap(List<LandMark> list) {
        Map<String, LandMark> output = new HashMap<>();
        for(LandMark l: list) {
            output.put(l.getId(), l);
        }
        return output;
    }

}
