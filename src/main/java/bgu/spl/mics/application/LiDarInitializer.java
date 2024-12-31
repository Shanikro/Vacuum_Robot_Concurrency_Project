package bgu.spl.mics.application;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.services.LiDarService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class LiDarInitializer {

//    public static List<MicroService> initializeLidars(JsonArray lidarData, String lidarDataPath) {
//
//        List<MicroService> lidarServices = new ArrayList<>();
//        List<LiDarWorkerTracker> lidars = new ArrayList<>();
//
//        for (int i = 0; i < lidarData.size(); i++) {
//            JsonObject lidarConfig = lidarData.get(i).getAsJsonObject();
//            int id = lidarConfig.get("id").getAsInt();
//            int frequency = lidarConfig.get("frequency").getAsInt();
//
//            //
//            LiDarWorkerTracker lidar = new LiDarWorkerTracker(id, frequency);
//            lidars.add(lidar);
//
//            //
//            MicroService lidarService = new LiDarService(lidar);
//            lidarServices.add(lidarService);
//        }
//
//        // initiate the Database
//        LiDarDataLoader.loadLiDarData(lidarDataPath);
//
//        return lidarServices;
//    }
public static List<MicroService> initializeLidars(List<LidarConfiguration> lidarConfigurations, String lidarDataPath) {

    List<MicroService> lidarServices = new ArrayList<>();
    List<LiDarWorkerTracker> lidars = new ArrayList<>();

    // עבור כל הגדרה של LiDAR
    for (LidarConfiguration lidarConfig : lidarConfigurations) {
        // קבלת הנתונים מתוך LidarConfiguration
        int id = lidarConfig.getId();
        int frequency = lidarConfig.getFrequency();

        // יצירת אובייקט LiDarWorkerTracker
        LiDarWorkerTracker lidar = new LiDarWorkerTracker(id, frequency);
        lidars.add(lidar);

        // יצירת שירות LiDAR
        MicroService lidarService = new LiDarService(lidar);
        lidarServices.add(lidarService);
    }

    // ייזום מסד הנתונים
    LiDarDataLoader.loadLiDarData(lidarDataPath);

    return lidarServices;
}


    private static List<StampedCloudPoints> extractPointClouds(JsonObject lidarConfig) {
        JsonArray pointCloudsArray = lidarConfig.getAsJsonArray("cloudPoints");
        List<StampedCloudPoints> pointCloudsList = new ArrayList<>();

        for (int j = 0; j < pointCloudsArray.size(); j++) {
            JsonObject stampedCloudConfig = pointCloudsArray.get(j).getAsJsonObject();
            int time = stampedCloudConfig.get("time").getAsInt();
            String objectId = stampedCloudConfig.get("id").getAsString();

            List<List<Double>> points = new ArrayList<>();
            JsonArray pointsArray = stampedCloudConfig.getAsJsonArray("cloudPoints");

            for (int k = 0; k < pointsArray.size(); k++) {
                JsonArray point = pointsArray.get(k).getAsJsonArray();
                List<Double> coordinates = new ArrayList<>();
                for (int l = 0; l < point.size(); l++) {
                    coordinates.add(point.get(l).getAsDouble());
                }
                points.add(coordinates);
            }

            pointCloudsList.add(new StampedCloudPoints(objectId, time, points));
        }
        return pointCloudsList;
    }
}
