package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class LiDarDataLoader {

    /**
     * Loads LiDAR data from a JSON file into the singleton LiDarDataBase.
     *
     * @param filePath Path to the JSON file containing LiDAR data.
     */
    public static void loadLiDarData( String filePath) {

        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            JsonArray lidarDataArray = gson.fromJson(reader, JsonArray.class);

            LiDarDataBase lidarDataBase = LiDarDataBase.getInstance();

            // Parse each LiDAR entry and add to database
            for (int i = 0; i < lidarDataArray.size(); i++) {
                JsonObject stampedCloudConfig = lidarDataArray.get(i).getAsJsonObject();

                int time = stampedCloudConfig.get("time").getAsInt();
                String objectId = stampedCloudConfig.get("id").getAsString();

                List<List<Double>> points = parseCloudPoints(stampedCloudConfig.getAsJsonArray("cloudPoints"));

                StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(objectId, time, points);
                lidarDataBase.addCloudPoints(stampedCloudPoints);
            }

            System.out.println("LiDAR data loaded successfully into the database.");

        } catch (Exception e) {
            System.err.println("Failed to load LiDAR data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses a JSON array of 3D points into a list of coordinate lists.
     *
     * @param cloudPointsArray The JSON array of cloud points.
     * @return A list of 3D points, each represented as a list of doubles.
     */
    private static List<List<Double>> parseCloudPoints(JsonArray cloudPointsArray) {

        List<List<Double>> points = new ArrayList<>();
        for (int i = 0; i < cloudPointsArray.size(); i++) {
            JsonArray point = cloudPointsArray.get(i).getAsJsonArray();
            List<Double> coordinates = new ArrayList<>();
            for (int j = 0; j < point.size(); j++) {
                coordinates.add(point.get(j).getAsDouble());
            }
            points.add(coordinates);
        }
        return points;
    }

}
