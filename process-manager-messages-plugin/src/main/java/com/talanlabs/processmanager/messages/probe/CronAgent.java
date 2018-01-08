package com.talanlabs.processmanager.messages.probe;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.messages.exceptions.AlreadyStartedProbeException;
import com.talanlabs.processmanager.shared.PluggableChannel;
import it.sauronsoftware.cron4j.Scheduler;
import java.io.Serializable;

public class CronAgent implements ProbeAgent {

    private final String channel;

    private final String beat;

    private final String schedulingPattern;

    private Scheduler scheduler;

    /**
     * @param channel           name of the channel on which a handle will be done
     * @param beat              message sent to the channel
     * @param schedulingPattern message sent to the channel. <a href="http://www.sauronsoftware.it/projects/cron4j/manual.php">See cron4j manual</a>
     */
    public CronAgent(String channel, String beat, String schedulingPattern) {
        super();

        this.channel = channel;
        this.beat = beat;
        this.schedulingPattern = schedulingPattern;
    }

    @Override
    public void work(Serializable message, String engineUuid) {
        synchronized (this) {
            if (STOP_MESSAGE == message) {
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
        if (scheduler != null) {
            throw new AlreadyStartedProbeException(getChannel());
        }
        PluggableChannel pluggableChannel = new ProcessingChannel("HEARTBEAT_" + channel, 1, this);
        ProcessManager.getEngine(engineUuid).plugChannel(pluggableChannel);
        pluggableChannel.activate(engineUuid);

        scheduler = new Scheduler();
        scheduler.setDaemon(true);
        scheduler.schedule(schedulingPattern, () -> ProcessManager.handle(engineUuid, channel, beat));
        scheduler.start();
    }

    @Override
    public void shutdown() {
        if (scheduler != null) {
            scheduler.stop();
            scheduler = null;
        }
    }
}
