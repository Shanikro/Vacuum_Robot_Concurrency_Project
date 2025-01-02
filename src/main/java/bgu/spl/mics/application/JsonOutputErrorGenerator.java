package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonOutputErrorGenerator {
    private String error;
    private String faultySensor;
    private Map<String,List<StampedDetectedObjects>> lastCamerasFrame;
    private Map<String,List<TrackedObject>> lastLiDarWorkerTrackersFrame;
    private List<Pose> poses;
    private StatisticalFolderAndLandmarks statistics;

    public JsonOutputErrorGenerator(Object object, List<Pose> poses ,StatisticalFolderAndLandmarks statistics ) {
        this.poses = poses;
        this.statistics = statistics;

        //error filed, faultySensor filed
        if (object instanceof Camera) {
            Camera camera = (Camera) object;
            this.error = camera.getError();
            this.faultySensor = "Camera" + camera.getId();
        }

        else{
            LiDarWorkerTracker LiDar = (LiDarWorkerTracker) object;
            this.faultySensor = "LiDarWorker" + LiDar.getId();
            error = "Sensor " + faultySensor + " disconnected";
        }

        //lastCamerasFrame filed, lastLiDarWorkerTrackersFrame filed



    }

    public void create() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter("error_output.json")) {
            // Serialize Java objects to JSON file
            gson.toJson(this, writer);
            System.out.println("FusionSlam data has been written to error_output.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
