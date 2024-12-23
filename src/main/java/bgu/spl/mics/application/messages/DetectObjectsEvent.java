package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;

import java.util.List;

public class DetectObjectsEvent implements Event<Boolean> { //TODO

        private final String senderName;
        private List<DetectedObject> detectedObjects;

        public DetectObjectsEvent(String senderName, int time, List<DetectedObject> detectedObjects){
                this.senderName = senderName;
                this.detectedObjects = detectedObjects;
        }


}
