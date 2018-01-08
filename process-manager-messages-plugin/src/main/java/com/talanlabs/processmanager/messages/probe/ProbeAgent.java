package com.talanlabs.processmanager.messages.probe;

import com.talanlabs.processmanager.shared.Agent;

public interface ProbeAgent extends Agent {

    enum SupportedMessages {

        /**
         * To stop the probe agent, you can either send STOP_MESSAGE to the heartbeat or cron channel (HeartbeatAgent_channelName or CronAgent_channelName) or you can use #shutdown
         */
        STOP
    }

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

    /**
     * Returns true if the probe is active, false otherwise
     */
    boolean isActive();

}
