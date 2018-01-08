package com.talanlabs.processmanager.messages.probe;

import com.talanlabs.processmanager.shared.Agent;

public interface ProbeAgent extends Agent {

    /**
     * To stop the probe agent, you can either send STOP_MESSAGE to the heartbeat channel (HEARTBEAT_channelName) or you can use #shutdown
     */
    String STOP_MESSAGE = "STOP";

    /**
     * Returns the name of the channel on which the message has to be send
     */
    String getChannel();

    /**
     * Activates the probe by registering a channel on the given engine
     */
    void activate(String engineUuid);

    /**
     * Shuts down the probe agent
     */
    void shutdown();

}
