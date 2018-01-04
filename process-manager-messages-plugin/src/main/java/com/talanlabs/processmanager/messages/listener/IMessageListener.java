package com.talanlabs.processmanager.messages.listener;

public interface IMessageListener {

    void messageTreated(Status status);

    enum Status {
        ACCEPTED, REJECTED;
    }

}
