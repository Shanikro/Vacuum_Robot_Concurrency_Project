package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.List;

public class DetectObjectsEvent implements Event<Boolean> {

        private final String senderName;
        private StampedDetectedObjects detectedObjects;

        public DetectObjectsEvent(String senderName, StampedDetectedObjects detectedObjects){
                this.senderName = senderName;
                this.detectedObjects = detectedObjects;
        }

        public String getSenderName() {
                return senderName;
        }

        public StampedDetectedObjects getDetectedObjects() {
                return detectedObjects;
        }


}
