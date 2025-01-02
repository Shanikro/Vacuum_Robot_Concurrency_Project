package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
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
import java.util.Map;

public class CameraParser {

    public static List<MicroService> initCameras(JsonObject config, String basePath) throws IOException {
        Gson gson = new Gson();
        JsonObject camerasConfig = config.getAsJsonObject("Cameras");
        JsonArray cameraConfigs = camerasConfig.getAsJsonArray("CamerasConfigurations");
        String cameraDataPath = basePath + "\\" + camerasConfig.get("camera_datas_path").getAsString();

        Type cameraDataType = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();
        Map<String, List<StampedDetectedObjects>> cameraData = gson.fromJson(new FileReader(cameraDataPath), cameraDataType);

        List<MicroService> cameraServices = new LinkedList<>();
        List<Camera> cameras = new ArrayList<>();
        for (JsonElement cameraElement : cameraConfigs) {
            JsonObject camConfig = cameraElement.getAsJsonObject();
            int id = camConfig.get("id").getAsInt();
            int frequency = camConfig.get("frequency").getAsInt();
            String key = camConfig.get("camera_key").getAsString();

            Camera newCamera = new Camera(id, frequency, cameraData.get(key));
            cameras.add(newCamera);
            cameraServices.add(new CameraService(newCamera));
        }


        System.out.println("Cameras initialized: " + cameras.size());

        return cameraServices;
    }
}
