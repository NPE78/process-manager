package com.talanlabs.processmanager.messages.exceptions;

public class AlreadyStartedProbeException extends RuntimeException {

    public AlreadyStartedProbeException(String channelName) {
        super("The probe " + channelName + " has already been started!");
    }
}
