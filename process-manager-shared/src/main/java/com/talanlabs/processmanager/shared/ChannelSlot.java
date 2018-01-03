package com.talanlabs.processmanager.shared;

import java.io.Serializable;
import java.util.List;

public interface ChannelSlot extends Channel {

    /**
     * Plug a pluggable channel to this channel slot. If the channel is available and there are stored messages in the channel slot, they will be processed
     */
    void plugChannel(PluggableChannel channel);

    /**
     * Unplug the current pluggable channel
     */
    void unplugChannel();

    /**
     * Is the channel slot plugged to a pluggable channel?
     * @return true if a pluggable channel is plugged, false otherwise
     */
    boolean isPlugged();

    /**
     * At least one agent is working
     */
    boolean isBusy();

    PluggableChannel getPluggedChannel();


    /**
     * Method to ask the channel to process a message.
     *
     * @param message the message to process
     * @return true if this channel is available.
     */
    HandleReport acceptMessage(Serializable message);

    /**
     * Returns the list of buffered messages to dump on the file system when shutting down
     */
    List<Serializable> getBufferedMessages();

    /**
     * Returns the number of buffered messages stored in the list
     */
    int getBufferedMessagesCount();

    /**
     * Store a message on the file system to back it up
     * @param foldername folder where the message must be saved
     * @param message message to dump
     * @param cpt A simple count to get a different file name for each stored message
     */
    void storeBufferedMessage(String foldername, Serializable message, int cpt);

    /**
     * Clear the list of saved messages
     */
    void clearSavedMessages();

}
