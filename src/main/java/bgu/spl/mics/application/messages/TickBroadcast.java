package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.DetectedObject;

import java.util.List;

public class TickBroadcast implements Broadcast {

    private String senderId;
    private int tick;
    private int duration;

    public TickBroadcast(String senderId,int tick,int duration) {
        this.senderId = senderId;
        this.tick = tick;
        this.duration = duration;
    }

    public String getSenderId() {
        return senderId;
    }

    public int getTime() {
        return tick;
    }

    public int getDuration(){
        return duration;
    }
}
