package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.Pose;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class PoseParser {
    public static List<Pose> loadPoseData(JsonObject config) throws IOException {
        Gson gson = new Gson();
        String posePath = config.get("poseJsonFile").getAsString();
        Type poseType = new TypeToken<List<Pose>>() {}.getType();
        posePath = "C:\\Users\\gayaa\\Downloads\\Skeleton\\example_input_2\\pose_data.json";
        List<Pose> poses = gson.fromJson(new FileReader(posePath), poseType);
        System.out.println("Poses loaded: " + poses.size());

        return poses;
    }
}
