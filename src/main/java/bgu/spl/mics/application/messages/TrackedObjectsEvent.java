package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

public class TrackedObjectsEvent implements Event<Boolean> {

    private final String senderName;
    private List<TrackedObject> trackedObjects;

    public TrackedObjectsEvent(String senderName, List<TrackedObject> detectedObjects){
        this.senderName = senderName;
        this.trackedObjects = detectedObjects;
    }

    public String getSenderName() {
        return senderName;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

}
