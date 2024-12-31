package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.DetectedObject;

import java.util.List;

public class TickBroadcast implements Broadcast {

    private String senderId;
    private int tick;

    public TickBroadcast(String senderId,int tick) {
        this.senderId = senderId;
        this.tick = tick;
    }

    public String getSenderId() {
        return senderId;
    }

    public int getTime() {
        return tick;
    }
}
