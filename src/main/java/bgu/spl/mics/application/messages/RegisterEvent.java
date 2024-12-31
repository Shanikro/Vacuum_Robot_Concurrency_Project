package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

public class RegisterEvent implements Event<Boolean> {

    private final String senderName;

    public RegisterEvent(String senderName){
        this.senderName = senderName;

    }

    public String getSenderName() {
        return senderName;
    }
}
