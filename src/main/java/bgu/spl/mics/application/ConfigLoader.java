package bgu.spl.mics.application;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Map;

public class ConfigLoader {

    public static Map<String, Object> loadConfig(String configFolderPath) throws Exception {

        Gson gson = new Gson();
        String configFilePath = Paths.get(configFolderPath, "configuration_file.json").toString();
        return gson.fromJson(new FileReader(configFilePath), Map.class);
    }

    public static JsonArray loadPoseData(String poseFilePath) throws Exception {
        Gson gson = new Gson();
        return gson.fromJson(new FileReader(poseFilePath), JsonArray.class);
    }
}

