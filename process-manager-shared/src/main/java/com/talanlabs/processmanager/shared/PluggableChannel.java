package com.talanlabs.processmanager.shared;

import java.io.Serializable;

public interface PluggableChannel extends Channel {

    /**
     * Method to ask the channel to process a message.
     *
     * @param message the message to process
     * @return a report about the result of the message processing operation.<br/>
     * If it is a {@link DelayedHandleReport}, the message will be played again later
     */
    HandleReport acceptMessage(Serializable message, ChannelSlot slot);

    /**
     * Shuts down the pluggable channel
     */
    void shutdown();

    boolean isOverloaded();

}
