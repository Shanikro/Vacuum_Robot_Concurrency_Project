package bgu.spl.mics.application;


import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Simulation.CameraParser;
import bgu.spl.mics.application.Simulation.LiDarParser;
import bgu.spl.mics.application.Simulation.PoseParser;
import bgu.spl.mics.application.Simulation.SimulationManager;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

        Gson gson = new Gson();

        try (FileReader configReader = new FileReader(configFolderPath)) {
            // Read main configuration file
            JsonObject config = JsonParser.parseReader(configReader).getAsJsonObject();

            File configFile = new File(configFolderPath);
            String basePath = configFile.getParent(); // Get the parent directory of the config file

            // Initialize Cameras
            List<MicroService> cameraServices = CameraParser.initCameras(config,basePath);

            // Load LiDar DataBase
            LiDarParser.loadLiDarDatabase(config, basePath);

            // Initialize LiDar Workers
            List<MicroService> LiDarServices = LiDarParser.initLiDarWorkers(config);


            // Load Pose Data
            List<Pose> poses = PoseParser.loadPoseData(config, basePath);

            // Load TickTime and Duration
            int tickTime = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();

            MicroService poseService = new PoseService(new GPSIMU(0,poses));

            SimulationManager.startSimulation(tickTime, duration, cameraServices, LiDarServices, poseService);

            System.out.println("Simulation has started!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String promptUserForPath() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please provide the path to the configuration folder:");
        return scanner.nextLine();
    }

}
