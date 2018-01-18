package com.talanlabs.processmanager.messages.probe;

import com.talanlabs.processmanager.engine.PM;
import it.sauronsoftware.cron4j.Scheduler;

public class CronAgent extends AbstractProbeAgent {

    private final String beat;

    private final String schedulingPattern;

    private Scheduler scheduler;

    /**
     * @param channel           name of the channel on which a handle will be done
     * @param beat              message sent to the channel
     * @param schedulingPattern message sent to the channel. <a href="http://www.sauronsoftware.it/projects/cron4j/manual.php">See cron4j manual</a>
     */
    public CronAgent(String channel, String beat, String schedulingPattern) {
        super(channel);

        this.beat = beat;
        this.schedulingPattern = schedulingPattern;
    }

    @Override
    public boolean isActive() {
        return scheduler != null;
    }

    @Override
    public void activateProbe(String engineUuid) {
        scheduler = new Scheduler();
        scheduler.setDaemon(true);
        scheduler.schedule(schedulingPattern, () -> PM.handle(engineUuid, getChannel(), beat));
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
