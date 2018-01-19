package com.talanlabs.processmanager.messages.probe;

import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.messages.exceptions.AlreadyStartedProbeException;
import java.io.Serializable;

public abstract class AbstractProbeAgent implements ProbeAgent {

    private final String channel;

    public AbstractProbeAgent(String channel) {
        this.channel = channel;
    }

    @Override
    public void work(Serializable message) {
        synchronized (this) {
            if (SupportedMessages.STOP == message) {
                shutdown();
            }
            // else, we ignore that message
        }
    }

    @Override
    public String getChannel() {
        return channel;
    }

    @Override
    public void activate(String engineUuid) {
        if (isActive()) {
            throw new AlreadyStartedProbeException(getChannel());
        }
        initChannel(engineUuid);

        activateProbe(engineUuid);
    }

    private void initChannel(String engineUuid) {
        ProcessingChannel pluggableChannel = new ProcessingChannel(getClass().getSimpleName() + "_" + getChannel(), 1, this) {
            @Override
            public void shutdown() {
                AbstractProbeAgent.this.shutdown();
            }
        };

        PM.getEngine(engineUuid).plugChannel(pluggableChannel);
        pluggableChannel.activate();
    }

    /**
     * The probe is not active, we can activate it
     */
    public abstract void activateProbe(String engineUuid);

}
