package bgu.spl.mics.application;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class CameraInitializer {
//    public static List<MicroService> initializeCameras(JsonArray cameraData) {
//
//        List<MicroService> cameraServices = new ArrayList<>();
//        List<Camera> cameras = new ArrayList<>();
//
//        for (int i = 0; i < cameraData.size(); i++) {
//            JsonObject cameraConfig = cameraData.get(i).getAsJsonObject();
//            int id = cameraConfig.get("id").getAsInt();
//            int frequency = cameraConfig.get("frequency").getAsInt();
//
//            List<StampedDetectedObjects> detectedObjectsList = extractDetectedObjects(cameraConfig);
//            Camera camera = new Camera(id, frequency, detectedObjectsList);
//            cameras.add(camera);
//
//            MicroService cameraService = new CameraService(camera);
//            cameraServices.add(cameraService);
//        }
//        return cameraServices;
//    }
public static List<MicroService> initializeCameras(List<CameraConfiguration> cameraConfigurations) {

    List<MicroService> cameraServices = new ArrayList<>();
    List<Camera> cameras = new ArrayList<>();

    // עבור כל הגדרה של מצלמה
    for (CameraConfiguration cameraConfig : cameraConfigurations) {
        // קבלת הנתונים מתוך CameraConfiguration
        int id = cameraConfig.getId();
        int frequency = cameraConfig.getFrequency();

        // הנחת ש- CameraConfiguration מכילה את רשימת ה- StampedDetectedObjects
        List<StampedDetectedObjects> detectedObjectsList = cameraConfig.getDetectedObjects();

        // יצירת אובייקט מצלמה
        Camera camera = new Camera(id, frequency, detectedObjectsList);
        cameras.add(camera);

        // יצירת שירות מצלמה
        MicroService cameraService = new CameraService(camera);
        cameraServices.add(cameraService);
    }

    return cameraServices;
}


    private static List<StampedDetectedObjects> extractDetectedObjects(JsonObject cameraConfig) {
        JsonArray detectedObjectsArray = cameraConfig.getAsJsonArray("detectedObjects");
        List<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();

        for (int j = 0; j < detectedObjectsArray.size(); j++) {
            JsonObject stampedObjectConfig = detectedObjectsArray.get(j).getAsJsonObject();
            int time = stampedObjectConfig.get("time").getAsInt();

            List<DetectedObject> objects = new ArrayList<>();
            JsonArray objectsArray = stampedObjectConfig.getAsJsonArray("detectedObjects");

            for (int k = 0; k < objectsArray.size(); k++) {
                JsonObject objectConfig = objectsArray.get(k).getAsJsonObject();
                String objectId = objectConfig.get("id").getAsString();
                String description = objectConfig.get("description").getAsString();
                objects.add(new DetectedObject(objectId, description));
            }

            detectedObjectsList.add(new StampedDetectedObjects(time, objects));
        }
        return detectedObjectsList;
    }
}

