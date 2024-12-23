package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminatedBroadcast implements Broadcast {

    private String senderId;

    public TerminatedBroadcast(String senderId) {
        this.senderId = senderId;
    }
}
