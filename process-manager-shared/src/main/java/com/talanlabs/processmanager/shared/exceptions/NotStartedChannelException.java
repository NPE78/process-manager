package com.talanlabs.processmanager.shared.exceptions;

public class NotStartedChannelException extends RuntimeException {

    public NotStartedChannelException(String channelName) {
        super(channelName + " channel is not started yet!");
    }
}
