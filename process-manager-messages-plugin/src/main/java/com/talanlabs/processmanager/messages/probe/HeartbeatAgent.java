package com.talanlabs.processmanager.messages.probe;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.messages.exceptions.AlreadyStartedProbeException;
import com.talanlabs.processmanager.shared.PluggableChannel;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.Serializable;

/**
 * Heartbeat agent which handles a message on a specified channel
 */
public class HeartbeatAgent implements ProbeAgent {

    private final String channel;

    private final String beat;

    private final long delay;

    private HeartbeatProbe probe;

    /**
     * @param channel name of the channel on which a handle will be done
     * @param beat    message sent to the channel
     * @param delay   delay in ms
     */
    public HeartbeatAgent(String channel, String beat, long delay) {
        super();

        this.channel = channel;
        this.beat = beat;
        this.delay = delay;
    }

    @Override
    public void work(Serializable message, String engineUuid) {
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
        if (probe != null) {
            throw new AlreadyStartedProbeException(getChannel());
        }
        PluggableChannel pluggableChannel = new ProcessingChannel("HEARTBEAT_" + channel, 1, this);
        ProcessManager.getEngine(engineUuid).plugChannel(pluggableChannel);
        pluggableChannel.activate(engineUuid);

        probe = new HeartbeatProbe(engineUuid, delay, channel, beat);
        probe.start();
    }

    @Override
    public void shutdown() {
        if (probe != null) {
            probe.disable();
            probe = null;
        }
    }

    /**
     * Heartbeat daemon thread which handles a message on a specified channel
     *
     * @see HeartbeatAgent
     */
    private class HeartbeatProbe extends Thread {

        private final String engineUuid;

        private final long delay;

        private final String channel;

        private final String beat;

        private final LogService logService;

        private boolean active;

        private HeartbeatProbe(String engineUuid, long delay, String channel, String beat) {
            super();

            logService = LogManager.getLogService(getClass());

            this.engineUuid = engineUuid;
            this.delay = delay;
            this.channel = channel;
            this.beat = beat;

            setDaemon(true);
        }

        @Override
        public void run() {
            active = true;
            try {
                sleep(delay);
                do {
                    ProcessManager.handle(engineUuid, channel, beat);
                    sleep(delay);
                } while (active);
            } catch (InterruptedException e) {
                logService.debug(() -> "Error with HeartbeatProbe on channel {0}", e, channel);
            }
        }

        private void disable() {
            this.active = false;
        }
    }
}
