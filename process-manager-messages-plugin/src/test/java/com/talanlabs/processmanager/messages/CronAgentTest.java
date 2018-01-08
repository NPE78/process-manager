package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.messages.exceptions.AlreadyStartedProbeException;
import com.talanlabs.processmanager.messages.probe.CronAgent;
import com.talanlabs.processmanager.messages.probe.ProbeAgent;
import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CronAgentTest {

    private final File basePath;

    public CronAgentTest() throws IOException {
        File tempFile = File.createTempFile("heartbeatAgentTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        basePath = new File(tmpFolder, UUID.randomUUID().toString());
        basePath.mkdir();

        tempFile.deleteOnExit();
        basePath.deleteOnExit();
    }

    @Test
    public void testCron() throws BaseEngineCreationException, InterruptedException {
        Engine engine = ProcessManager.getInstance().createEngine("testCron", basePath);
        CronAgent agent;
        try {
            agent = new CronAgent("myChannel", "BEAT", "* * * * *");

            engine.plugChannel(new MyCronChannel((message, engineUuid) -> {
            }));
            engine.activateChannels();

            Assertions.assertThat(agent.isActive()).isFalse();
            agent.activate(engine.getUuid());
            Assertions.assertThat(agent.isActive()).isTrue();

            sleep(10);

        } finally {
            engine.shutdown();
        }
        Assertions.assertThat(agent.isActive()).isFalse();
    }

    @Test(expected = AlreadyStartedProbeException.class)
    public void testCronTwice() throws BaseEngineCreationException {
        Engine engine = ProcessManager.getInstance().createEngine("testCron", basePath);
        CronAgent agent;
        try {
            agent = new CronAgent("myChannel", "BEAT", "* * * * *");
            agent.activate(engine.getUuid());
            agent.activate(engine.getUuid());

            Assertions.assertThat(agent.isActive()).isTrue();
            engine.handle("CronAgent_myChannel", ProbeAgent.SupportedMessages.STOP);
            Assertions.assertThat(agent.isActive()).isFalse();
        } finally {
            engine.shutdown();
        }
    }

    @Test
    public void testCronStopHandle() throws BaseEngineCreationException, InterruptedException {
        Engine engine = ProcessManager.getInstance().createEngine("testCron", basePath);
        CronAgent agent;
        try {
            agent = new CronAgent("myChannel", "BEAT", "* * * * *");
            agent.activate(engine.getUuid());

            Assertions.assertThat(agent.isActive()).isTrue();
            engine.handle("CronAgent_myChannel", ProbeAgent.SupportedMessages.STOP);

            sleep(50);
            Assertions.assertThat(agent.isActive()).isFalse();
        } finally {
            engine.shutdown();
        }
    }

    // Utilities and classes

    private void sleep(int ms) throws InterruptedException {
        new CountDownLatch(1).await(ms, TimeUnit.MILLISECONDS);
    }

    private class MyCronChannel extends ProcessingChannel {

        public MyCronChannel(Agent agent) {
            super("myChannel", 1, agent);
        }
    }
}
