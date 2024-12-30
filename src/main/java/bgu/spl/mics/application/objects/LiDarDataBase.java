package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;

import java.util.LinkedList;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private List<StampedCloudPoints> cloudPoints;

    private LiDarDataBase(){
        this.cloudPoints = new LinkedList<>();
    }

    //Internal static class that holds the Singleton
    private static class singletonHolder {
        private static final LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        return singletonHolder.INSTANCE;
    }

    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }
}
