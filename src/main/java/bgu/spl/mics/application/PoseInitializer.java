package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.services.PoseService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PoseInitializer {

    public static MicroService initializePose(JsonArray poseData) {

        List<Pose> poses = new ArrayList<>();

        // init poses
        for (int i = 0; i < poseData.size(); i++) {
            JsonObject poseConfig = poseData.get(i).getAsJsonObject();
            int time = poseConfig.get("time").getAsInt();
            float x = poseConfig.get("x").getAsFloat();
            float y = poseConfig.get("y").getAsFloat();
            float yaw = poseConfig.get("yaw").getAsFloat();
            Pose pose = new Pose(x, y, yaw, time);
            poses.add(pose);
        }
        MicroService poseService = new PoseService(new GPSIMU(0, poses));

        return poseService;

    }
}
