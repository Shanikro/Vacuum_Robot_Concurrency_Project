package bgu.spl.mics.application;


import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.IOException;
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
    public static void main(String[] args) {
            String configFolderPath = args.length > 0 ? args[0] : promptUserForPath();

            try {
                Map<String, Object> configData = ConfigLoader.loadConfig(configFolderPath);
                JsonArray poseData = ConfigLoader.loadPoseData((String) configData.get("poseJsonFile"));

                JsonArray cameraData = new Gson().toJsonTree(configData.get("Cameras.CamerasConfigurations")).getAsJsonArray();
                JsonArray lidarData = new Gson().toJsonTree(configData.get("LiDarWorkers.LidarConfigurations")).getAsJsonArray();

                int tickTime = ((Double) configData.get("TickTime")).intValue();
                int duration = ((Double) configData.get("Duration")).intValue();

                List<MicroService> cameraServices = CameraInitializer.initializeCameras(cameraData);
                List<MicroService> lidarServices = LiDarInitializer.initializeLidars(lidarData);
                MicroService poseService = PoseInitializer.initializePose(poseData);

                SimulationManager.startSimulation(tickTime, duration, cameraServices, lidarServices, poseService);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static String promptUserForPath () {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please provide the path to the configuration folder:");
            return scanner.nextLine();
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
            List<MicroService> cameraServices = initializeCameras(cameraConfigsArray);
            List<MicroService> lidarServices = initializeLidars(lidarConfigsArray);
            MicroService poseService = initializePose(poseData);

            MicroService timeService = new TimeService(tickTime,duration);
            new Thread(timeService).start();



            MicroService fusionSlamService = new FusionSlamService(FusionSlam.getInstance());
            new Thread(fusionSlamService).start();




        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static MicroService initializePose(JsonArray poseData) {
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
        //new Thread(poseService).start(); //TODO: חרדה
        // pose data
        for (Pose pose : poses) {
            System.out.println("Pose Time: " + pose.getTime());
            System.out.println("Pose X: " + pose.getX());
            System.out.println("Pose Y: " + pose.getY());
            System.out.println("Pose Yaw: " + pose.getYaw());
        }
        return poseService;

    }

    private static List<MicroService> initializeLidars(JsonArray lidarData) {
        List<LiDarWorkerTracker> lidars = new ArrayList<>();
        List<MicroService> lidarServices = new ArrayList<>();

        // Initialize LiDARs
        for (int i = 0; i < lidarData.size(); i++) {
            JsonObject lidarConfig = lidarData.get(i).getAsJsonObject();

            // Read LiDAR-specific data
            int id = lidarConfig.get("id").getAsInt(); // LiDAR ID
            int frequency = lidarConfig.get("frequency").getAsInt(); // LiDAR frequency

            // Initialize the list of point clouds for this LiDAR
            JsonArray pointCloudsArray = lidarConfig.getAsJsonArray("cloudPoints");
            List<StampedCloudPoints> pointCloudsList = new ArrayList<>();

            for (int j = 0; j < pointCloudsArray.size(); j++) {
                JsonObject StampedCloudPointsConfig = pointCloudsArray.get(j).getAsJsonObject();
                int time = StampedCloudPointsConfig.get("time").getAsInt(); // Time of point cloud generation
                String objectId = StampedCloudPointsConfig.get("id").getAsString(); // ID of the detected object

                // Extract the list of 3D points for this point cloud
                JsonArray pointsArray = StampedCloudPointsConfig.getAsJsonArray("cloudPoints");
                List<List<Double>> points = new ArrayList<>();

                for (int k = 0; k < pointsArray.size(); k++) {
                    JsonArray point = pointsArray.get(k).getAsJsonArray();
                    double x = point.get(0).getAsDouble(); // X-coordinate
                    double y = point.get(1).getAsDouble(); // Y-coordinate
                    double z = point.get(2).getAsDouble(); // Z-coordinate
                    List<Double> list = new LinkedList<>();
                    list.add(x);
                    list.add(y);
                    list.add(z);
                    points.add(list); // Add the 3D point to the list
                }

                // Add the stamped point cloud to the list
                pointCloudsList.add(new StampedCloudPoints(objectId, time, points));
            }

            // Create the LiDarWorkerTracker object with all the extracted data
            LiDarWorkerTracker lidar = new LiDarWorkerTracker(id, frequency, pointCloudsList);
            lidars.add(lidar); // Add the LiDAR to the list

            // Create a service for the LiDAR and start it in a new thread
            MicroService LiDARService = new LiDarService(lidar);
            new Thread(LiDARService).start(); // Start the LiDAR service

        }
        // LiDAR data
        for (LiDarWorkerTracker lidar : lidars) {
            System.out.println("LiDAR ID: " + lidar.getId());
            System.out.println("LiDAR Frequency: " + lidar.getFrequency());
        }
        return lidarServices;
    }

    private static List<MicroService> initializeCameras(JsonArray cameraData) {

        List<Camera> cameras = new ArrayList<>();
        List<MicroService> cameraServices = new ArrayList<>();

        // Initialize cameras
        for (int i = 0; i < cameraData.size(); i++) {
            JsonObject cameraConfig = cameraData.get(i).getAsJsonObject();

            // Read camera-specific data
            int id = cameraConfig.get("id").getAsInt();
            int frequency = cameraConfig.get("frequency").getAsInt();

            // Initialize the list of detected objects for this camera
            JsonArray detectedObjectsArray = cameraConfig.getAsJsonArray("detectedObjects");
            List<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();

            for (int j = 0; j < detectedObjectsArray.size(); j++) {
                JsonObject stampedObjectConfig = detectedObjectsArray.get(j).getAsJsonObject();
                int time = stampedObjectConfig.get("time").getAsInt();
                // Extract the list of detected objects at this time
                JsonArray objectsArray = stampedObjectConfig.getAsJsonArray("detectedObjects");
                List<DetectedObject> objects = new ArrayList<>();

                for (int k = 0; k < objectsArray.size(); k++) {
                    JsonObject objectConfig = objectsArray.get(k).getAsJsonObject();
                    String objectId = objectConfig.get("id").getAsString();
                    String description = objectConfig.get("description").getAsString();
                    objects.add(new DetectedObject(objectId, description));
                }
                // Add timestamped detected objects
                detectedObjectsList.add(new StampedDetectedObjects(time, objects));
            }

            // Create the Camera object with all the extracted data
            Camera camera = new Camera(id, frequency, detectedObjectsList);
            cameras.add(camera);// no need?
            // Create a service for the camera and start it in a new thread
            MicroService cameraService = new CameraService(camera);
            cameraServices.add(cameraService);
            //new Thread(cameraService).start(); //TODO: חרדה
        }
        // camera data
        for (Camera camera : cameras) {
            System.out.println("Camera ID: " + camera.getId());
            System.out.println("Camera Frequency: " + camera.getFrequency());
        }
        return cameraServices;
    }


    private static void initializeSimulation(int tickTime, int duration, JsonArray cameraData, JsonArray lidarData, JsonArray poseData) {

        MicroService fusionSlamService = new FusionSlamService(FusionSlam.getInstance());
        new Thread(fusionSlamService).start();

        MicroService timeService = new TimeService(tickTime,duration);
        new Thread(timeService).start();



    }
}
