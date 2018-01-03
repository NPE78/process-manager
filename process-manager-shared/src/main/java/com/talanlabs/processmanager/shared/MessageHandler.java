package com.talanlabs.processmanager.shared;

import java.io.Serializable;

public interface MessageHandler {

    /**
     * Send a message to this handler.
     *
     * @param channelName name of the channel where to send the message.
     * @param message     instance of message to send for processing.
     * @return a report of the integration of the message
     */
    HandleReport handle(String channelName, Serializable message);

    /**
     * Is the channel available on this message handler
     *
     * @param channelName the name of the channel to check.
     * @return true or false whether it is available or not
     */
    boolean isAvailable(String channelName);

    /**
     * Is the channel busy on this message handler
     *
     * @param channelName the name of the channel to check.
     * @return true if busy, false if idling
     */
    boolean isBusy(String channelName);

    /**
     * Is the channel in overload?
     *
     * @param channelName the name of the channel to check.
     * @return true if overloaded, false otherwise
     */
    boolean isOverloaded(String channelName);

}
