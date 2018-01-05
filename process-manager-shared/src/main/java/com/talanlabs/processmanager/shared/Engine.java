package com.talanlabs.processmanager.shared;

public interface Engine extends MessageHandler {

    void plugChannel(PluggableChannel channel);

    void unplugChannel(String channelName);

    /**
     * Sets a engine listener to be notified
     */
    void setListener(EngineListener listener);

    /**
     * Called when the process manager engine must shut down
     */
    void shutdown();

    /**
     * Activate all channel slots
     */
    void activateChannels();

    /**
     * Sets a channel available or not. To be available, a pluggable channel has to be plugged in
     */
    void setAvailable(String channelName, boolean available);

    /**
     * Returns the number of working agents on the given channel
     */
    int getNbWorking(String channelName);

}
