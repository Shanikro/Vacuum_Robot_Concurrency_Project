package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticalFolderAndLandmarks {
    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private List<LandMark> landMarks;


    public StatisticalFolderAndLandmarks(StatisticalFolder statisticalFolder, List<LandMark> landMarks){
        this.landMarks = landMarks;
        this.systemRuntime = statisticalFolder.getSystemRuntime();
        this.numDetectedObjects = statisticalFolder.getNumDetectedObjects();
        this.numTrackedObjects = statisticalFolder.getNumTrackedObjects();
        this.numLandmarks = statisticalFolder.getNumLandmarks();

    }

}
