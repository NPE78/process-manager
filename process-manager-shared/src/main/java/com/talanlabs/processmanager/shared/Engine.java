package com.talanlabs.processmanager.shared;

import java.util.List;

public interface Engine extends MessageHandler {

    /**
     * Returns the unique id of the engine
     */
    String getUuid();

    void plugChannel(PluggableChannel channel);

    void unplugChannel(String channelName);

    /**
     * Sets a engine listener to be notified
     */
    void setListener(EngineListener listener);

    <V> void setProperty(EnginePropertyKey<V> key, V value);

    <V> V getProperty(EnginePropertyKey<V> key);

    /**
     * Returns the list of plugged channels
     */
    List<PluggableChannel> getPluggedChannels();

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

    interface EnginePropertyKey<V> {
        Class<V> getType();
    }
}
