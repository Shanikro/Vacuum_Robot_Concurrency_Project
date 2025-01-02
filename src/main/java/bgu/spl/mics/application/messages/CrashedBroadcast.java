package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {

    private String senderId;
    private Object object; //the object with the error

    public CrashedBroadcast(String senderId, Object object) {
        this.senderId = senderId;
        this.object = object;
    }

    public String getSenderId() {
        return senderId;
    }

    public Object getObject() {
        return object;
    }

}
