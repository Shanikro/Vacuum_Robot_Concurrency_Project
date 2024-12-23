package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.Pose;

import java.util.List;

public class PoseEvent implements Event<String> { //TODO

    private final String senderName;
    private final Pose pose;

    public PoseEvent(String senderName,Pose pose){
        this.senderName = senderName;
        this.pose = pose;
    }


}
