package bgu.spl.mics.application.Simulation;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.services.LiDarService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LiDarParser {
    public static List<MicroService> initLiDarWorkers(JsonObject config) throws IOException {
        Gson gson = new Gson();
        JsonObject lidarConfig = config.getAsJsonObject("LiDarWorkers");
        JsonArray lidarConfigs = lidarConfig.getAsJsonArray("LidarConfigurations");

        List<MicroService> LiDarServices = new LinkedList<>();
        List<LiDarWorkerTracker> lidarWorkers = new ArrayList<>();
        for (JsonElement lidarElement : lidarConfigs) {
            JsonObject lidarConfigObj = lidarElement.getAsJsonObject();
            int id = lidarConfigObj.get("id").getAsInt();
            int frequency = lidarConfigObj.get("frequency").getAsInt();

            LiDarWorkerTracker lidarWorker = new LiDarWorkerTracker(id, frequency);
            lidarWorkers.add(lidarWorker);
            LiDarServices.add(new LiDarService(lidarWorker));
        }
        return LiDarServices;
    }

    public static void loadLiDarDatabase(JsonObject config, String basePath) throws IOException {
        Gson gson = new Gson();
        String lidarDataPath = basePath + config.getAsJsonObject("LiDarWorkers").get("lidars_data_path").getAsString().substring(1);
        Type lidarDataType = new TypeToken<List<StampedCloudPoints>>() {}.getType();
        List<StampedCloudPoints> lidarData = gson.fromJson(new FileReader(lidarDataPath), lidarDataType);

        LiDarDataBase database = LiDarDataBase.getInstance();
        for (StampedCloudPoints point : lidarData) {
            database.addCloudPoints(point);
        }

        database.setStampedPointsUntilLiDarsFinish(database.getCloudPoints().size());
    }
}
