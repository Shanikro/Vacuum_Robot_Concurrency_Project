package bgu.spl.mics.application.Simulation;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.TimeService;

import java.util.List;

public class SimulationManager {

    public static void startSimulation(int tickTime, int duration, List<MicroService> cameraServices, List<MicroService> lidarServices, MicroService poseService) {

        //Update Sensors to handle
        int sensors = cameraServices.size() + lidarServices.size() + 1;
        StatisticalFolder.getInstance().setSensorsInAction(sensors);

        MicroService fusionSlamService = new FusionSlamService(FusionSlam.getInstance());
        new Thread(fusionSlamService).start();

        for (MicroService service : lidarServices) {
            new Thread(service).start();
        }

        for (MicroService service : cameraServices) {
            new Thread(service).start();
        }

        new Thread(poseService).start();

        MicroService timeService = new TimeService(tickTime, duration);
        new Thread(timeService).start();
    }
}
