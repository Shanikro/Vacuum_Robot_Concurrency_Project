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
    private int stampedPointsUntilLiDarsFinish;

    private LiDarDataBase(){
        this.cloudPoints = new LinkedList<>();
        this.stampedPointsUntilLiDarsFinish = 0;
    }

    //Internal static class that holds the Singleton
    private static class singletonHolder {
        private static final LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance() {
        return singletonHolder.INSTANCE;
    }

    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }

    public void addCloudPoints(StampedCloudPoints stampedCloudPoints) {
        cloudPoints.add(stampedCloudPoints);
    }

    public void setStampedPointsUntilLiDarsFinish(int size) {
        stampedPointsUntilLiDarsFinish = size;
    }

    public int getStampedPointsUntilLiDarsFinish() {
        return stampedPointsUntilLiDarsFinish;
    }
}
