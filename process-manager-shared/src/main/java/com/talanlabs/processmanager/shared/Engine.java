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

}
