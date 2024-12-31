package bgu.spl.mics.application;


import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
//    public static void main(String[] args) {
//            String configFolderPath = args.length > 0 ? args[0] : promptUserForPath();
//
//            try {
//                Map<String, Object> configData = ConfigLoader.loadConfig(configFolderPath);
//
//                String lidarJsonFilePath = (String) configData.get("lidars_data_path");
//                LiDarDataLoader.loadLiDarData(lidarJsonFilePath);
//
//                JsonArray poseData = ConfigLoader.loadPoseData((String) configData.get("poseJsonFile"));
//
//                JsonArray cameraData = new Gson().toJsonTree(configData.get("Cameras.CamerasConfigurations")).getAsJsonArray();
//                JsonArray lidarData = new Gson().toJsonTree(configData.get("LiDarWorkers.LidarConfigurations")).getAsJsonArray();
//                String lidarDataPath = (String) configData.get("LiDarWorkers.lidars_data_path");
//
//                int tickTime = ((Double) configData.get("TickTime")).intValue();
//                int duration = ((Double) configData.get("Duration")).intValue();
//
//                List<MicroService> cameraServices = CameraInitializer.initializeCameras(cameraData);
//                List<MicroService> lidarServices = LiDarInitializer.initializeLidars(lidarData, lidarDataPath);
//                MicroService poseService = PoseInitializer.initializePose(poseData);
//
//                SimulationManager.startSimulation(tickTime, duration, cameraServices, lidarServices, poseService);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        private static String promptUserForPath () {
//            Scanner scanner = new Scanner(System.in);
//            System.out.println("Please provide the path to the configuration folder:");
//            return scanner.nextLine();
//        }

//    public static void main(String[] args) {
//        String configFolderPath = args.length > 0 ? args[0] : promptUserForPath();
//
//        try {
//            // קריאה לקובץ קונפיגורציה
//            Map<String, Object> configData = ConfigLoader.loadConfig(configFolderPath);
//
//            // קריאה לקובץ pose_data.json עם Gson
//            String poseJsonFilePath = (String) configData.get("poseJsonFile").toString();
//            poseJsonFilePath = "C:\\Users\\gayaa\\Downloads\\Skeleton\\example_input_2\\pose_data.json";
//            JsonArray poseData = loadJsonArray(poseJsonFilePath);
//
//            // קריאת הגדרות מצלמה
//            String cameraConfigPath = (String) configData.get("Cameras.camera_datas_path");
//            cameraConfigPath ="C:\\Users\\gayaa\\Downloads\\Skeleton\\example_input_2\\camera_data.json";
//            //List<CameraConfiguration> cameraConfigurations = loadConfigurations(cameraConfigPath, new TypeToken<List<CameraConfiguration>>() {});
//            CameraConfiguration cameraConfigurations = loadConfigurations(cameraConfigPath, new TypeToken<List<CameraConfiguration>>() {});
//
//            // קריאת הגדרות LiDAR - כאן אנו טוענים את הגדרות ה-LiDAR מתוך קובץ JSON
//            String lidarConfigPath = (String) configData.get("LiDarWorkers.lidars_data_path");
//            lidarConfigPath ="C:\\Users\\gayaa\\Downloads\\Skeleton\\example_input_2\\lidar_data.json";
//            //List<LidarConfiguration> lidarConfigurations = loadConfigurations(lidarConfigPath, new TypeToken<List<LidarConfiguration>>() {});
//            LidarConfiguration lidarConfigurations = loadConfigurations(lidarConfigPath, new TypeToken<List<LidarConfiguration>>() {});
//
//            // קריאת הגדרות זמן הסימולציה
//            int tickTime = ((Double) configData.get("TickTime")).intValue();
//            int duration = ((Double) configData.get("Duration")).intValue();
//
//            // יצירת שירותים עבור מצלמות, LiDAR ו-Pose
////            List<MicroService> cameraServices = CameraInitializer.initializeCameras(cameraConfigurations);
////            List<MicroService> lidarServices = LiDarInitializer.initializeLidars(lidarConfigurations,lidarConfigPath); // שינוי פה
//            MicroService poseService = PoseInitializer.initializePose(poseData);
//
//            // הפעלת הסימולציה
//            //SimulationManager.startSimulation(tickTime, duration, cameraServices, lidarServices, poseService);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String promptUserForPath() {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Please provide the path to the configuration folder:");
//        return scanner.nextLine();
//    }
//
//    /**
//     * Helper method to load a JSON file into a JsonArray.
//     *
//     * @param filePath Path to the JSON file.
//     * @return JsonArray representing the JSON data.
//     * @throws IOException If an error occurs while reading the file.
//     */
//    private static JsonArray loadJsonArray(String filePath) throws IOException {
//        Gson gson = new Gson();
//        try (FileReader reader = new FileReader(filePath)) {
//            return gson.fromJson(reader, JsonArray.class);
//        }
//    }
//
//    /**
//     * Helper method to load JSON configurations into a List of objects.
//     *
//     * @param filePath Path to the JSON file.
//     * @param typeToken TypeToken for the target List of objects.
//     * @param <T> The type of objects in the list.
//     * @return List of objects parsed from the JSON file.
//     * @throws IOException If an error occurs while reading the file.
//     */
//    private static <T> T loadConfigurations(String filePath, TypeToken<List<T>> typeToken) throws IOException {
//        Gson gson = new Gson();
//        try (FileReader reader = new FileReader(filePath)) {
//            Type type = typeToken.getType();
//            return gson.fromJson(reader, type);
//        }
//    }
    public static void main(String[] args) {
        String configFolderPath = args.length > 0 ? args[0] : promptUserForPath();

        try {
            // קריאה לקובץ קונפיגורציה
            Map<String, Object> configData = ConfigLoader.loadConfig(configFolderPath);

            // קריאה לקובץ pose_data.json עם Gson
            String poseJsonFilePath = (String) configData.get("poseJsonFile");
            poseJsonFilePath = "C:\\Users\\gayaa\\Downloads\\Skeleton\\example_input_2\\pose_data.json";
            JsonArray poseData = loadJsonArray(poseJsonFilePath);

            // קריאת הגדרות מצלמה
            String cameraConfigPath = (String) configData.get("Cameras.camera_datas_path");
            cameraConfigPath = "C:\\Users\\gayaa\\Downloads\\Skeleton\\example_input_2\\camera_data.json";
            Map<String, List<CameraConfiguration>> cameraConfigurations = loadConfigurations(cameraConfigPath, new TypeToken<Map<String, List<CameraConfiguration>>>() {});

            // קריאת הגדרות LiDAR
            String lidarConfigPath = (String) configData.get("LiDarWorkers.lidars_data_path");
            lidarConfigPath = "C:\\Users\\gayaa\\Downloads\\Skeleton\\example_input_2\\lidar_data.json";
            List<LidarConfiguration> lidarConfigurations = loadConfigurations(lidarConfigPath, new TypeToken<List<LidarConfiguration>>() {});

            // קריאת הגדרות זמן הסימולציה
            int tickTime = ((Double) configData.get("TickTime")).intValue();
            int duration = ((Double) configData.get("Duration")).intValue();

            // יצירת שירותים עבור מצלמות, LiDAR ו-Pose
            List<CameraConfiguration> camera1Configurations = cameraConfigurations.get("camera1");
            List<MicroService> cameraServices = CameraInitializer.initializeCameras(camera1Configurations);
            List<MicroService> lidarServices = LiDarInitializer.initializeLidars(lidarConfigurations, lidarConfigPath);
            MicroService poseService = PoseInitializer.initializePose(poseData);

            // הפעלת הסימולציה
            SimulationManager.startSimulation(tickTime, duration, cameraServices, lidarServices, poseService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static <T> T loadConfigurations(String filePath, TypeToken<T> typeToken) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type type = typeToken.getType();
            return gson.fromJson(reader, type);
        }
    }
    private static String promptUserForPath() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please provide the path to the configuration folder:");
        return scanner.nextLine();
    }

    /**
     * Helper method to load a JSON file into a JsonArray.
     *
     * @param filePath Path to the JSON file.
     * @return JsonArray representing the JSON data.
     * @throws IOException If an error occurs while reading the file.
     */
    private static JsonArray loadJsonArray(String filePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, JsonArray.class);
        }
    }



}
