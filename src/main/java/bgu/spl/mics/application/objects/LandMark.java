package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;
import java.util.PrimitiveIterator;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {

    private String id;
    private String description;
    private List<CloudPoint> coordinates;

    public LandMark(String id, String description, List<CloudPoint> coordinates){

        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public List<CloudPoint> getCoordinates(){
        return coordinates;
    }

    public void addCoordinate(CloudPoint cloudPoint){
        coordinates.add(cloudPoint);
    }

    public void setCoordinates(List<CloudPoint> list) {
        coordinates = list;
    }
}
