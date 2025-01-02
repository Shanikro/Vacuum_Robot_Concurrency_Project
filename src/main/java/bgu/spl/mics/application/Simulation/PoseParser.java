package bgu.spl.mics.application.Simulation;

import bgu.spl.mics.application.objects.Pose;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class PoseParser {
    public static List<Pose> loadPoseData(JsonObject config, String basePath) throws IOException {
        Gson gson = new Gson();
        String posePath = basePath + "\\" + config.get("poseJsonFile").getAsString();
        Type poseType = new TypeToken<List<Pose>>() {}.getType();
        List<Pose> poses = gson.fromJson(new FileReader(posePath), poseType);
        System.out.println("Poses loaded: " + poses.size());

        return poses;
    }
}
