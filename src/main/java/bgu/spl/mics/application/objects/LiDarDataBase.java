package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;

import java.util.LinkedList;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private static LiDarDataBase instance = null;

    private List<StampedCloudPoints> cloudPoints;
    private String filePath;

    private LiDarDataBase(String filePath){
        this.filePath = filePath;
        this.cloudPoints = new LinkedList<>();
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static synchronized LiDarDataBase getInstance(String filePath) {
        //check if it's the right way
        if (instance == null) {
            instance = new LiDarDataBase(filePath);
        }
        return instance;
    }
}
