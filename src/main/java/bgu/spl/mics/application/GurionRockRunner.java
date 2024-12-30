package bgu.spl.mics.application;


import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.services.CameraService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
    public static void main(String[] args) {
        //TODO: כל מה שעם העערות בעברית צריך למחוק זה רק בשביל להריץ עכשיו
        // אם לא קיבלנו את הנתיב בשורת הפקודה, נשאל את המשתמש להכניס אותו
        String configFolderPath;

        if (args.length == 0) {
            // בקשה לקלט מהמשתמש אם לא הועבר ארגומנט
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please provide the path to the configuration folder:");
            configFolderPath = scanner.nextLine(); // קרא את הקלט מהמשתמש
        } else {
            // אם יש ארגומנט, השתמש בו כנתיב לתיקיית הקונפיגורציה
            configFolderPath = args[0];
        }

        // אם לא הוזן נתיב, הצג שגיאה ויצא
        if (configFolderPath.isEmpty()) {
            System.err.println("Please provide a valid path to the configuration folder.");
            return;
        }
        System.out.println("Configuration folder path: " + configFolderPath);// עד לפה

        initializeAndStartSimulation(configFolderPath);

    }

    private static void initializeAndStartSimulation(String configFolderPath) {

        Gson gson = new Gson();

        try {
            // Load the configuration file as a Map
            String configFilePath = Paths.get(configFolderPath, "configuration_file.json").toString();
            Map<String, Object> configData = gson.fromJson(new FileReader(configFilePath), Map.class);
            System.out.println("Configuration loaded successfully.");

            // Extract camera data from config (using the "Cameras" key)
            Map<String, Object> camerasData = (Map<String, Object>) configData.get("Cameras");
            JsonArray cameraConfigsArray = gson.toJsonTree(camerasData.get("CamerasConfigurations")).getAsJsonArray();
            System.out.println("Camera data loaded successfully.");

            // Extract LiDAR data from config (using the "LiDarWorkers" key)
            Map<String, Object> lidarData = (Map<String, Object>) configData.get("LiDarWorkers");
            JsonArray lidarConfigsArray = gson.toJsonTree(lidarData.get("LidarConfigurations")).getAsJsonArray();
            System.out.println("LiDAR data loaded successfully.");

            // Extract pose data from config
            String poseFilePath = (String) configData.get("poseJsonFile");
            JsonArray poseData = gson.fromJson(new FileReader(poseFilePath), JsonArray.class);
            System.out.println("Pose data loaded successfully.");

            // Extract TickTime and Duration
            int tickTime = ((Double) configData.get("TickTime")).intValue();
            int duration = ((Double) configData.get("Duration")).intValue();

            // Initialize the simulation with the extracted data
            initializeSimulation(tickTime, duration, cameraConfigsArray, lidarConfigsArray, poseData);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void initializeSimulation(int tickTime, int duration, JsonArray cameraData, JsonArray lidarData, JsonArray poseData) {

        List<Camera> cameras = new ArrayList<>();
        List<LiDarWorkerTracker> lidars = new ArrayList<>();
        List<Pose> poses = new ArrayList<>();


        // init camera
        for (int i = 0; i < cameraData.size(); i++) {
            JsonObject cameraConfig = cameraData.get(i).getAsJsonObject();
            int id = cameraConfig.get("id").getAsInt();
            int frequency = cameraConfig.get("frequency").getAsInt();
            Camera camera = new Camera(id, frequency);
            cameras.add(camera);
        }

        // init LiDar
        for (int i = 0; i < lidarData.size(); i++) {
            JsonObject lidarConfig = lidarData.get(i).getAsJsonObject();
            int id = lidarConfig.get("id").getAsInt();
            int frequency = lidarConfig.get("frequency").getAsInt();
            LiDarWorkerTracker lidar = new LiDarWorkerTracker(id, frequency);
            lidars.add(lidar);
        }

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

        // printings
        System.out.println("Initializing simulation with the following data:");
        System.out.println("TickTime: " + tickTime);
        System.out.println("Duration: " + duration);
        System.out.println("Number of cameras: " + cameras.size());
        System.out.println("Number of LiDARs: " + lidars.size());
        System.out.println("Number of poses: " + poses.size());

        // camera data
        for (Camera camera : cameras) {
            System.out.println("Camera ID: " + camera.getId());
            System.out.println("Camera Frequency: " + camera.getFrequency());
        }

        // LiDAR data
        for (LiDarWorkerTracker lidar : lidars) {
            System.out.println("LiDAR ID: " + lidar.getId());
            System.out.println("LiDAR Frequency: " + lidar.getFrequency());
        }
        // pose data
        for (Pose pose : poses) {
            System.out.println("Pose Time: " + pose.getTime());
            System.out.println("Pose X: " + pose.getX());
            System.out.println("Pose Y: " + pose.getY());
            System.out.println("Pose Yaw: " + pose.getYaw());
        }

    }
}
