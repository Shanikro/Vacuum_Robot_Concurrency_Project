package bgu.spl.mics.application.objects;

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

}
