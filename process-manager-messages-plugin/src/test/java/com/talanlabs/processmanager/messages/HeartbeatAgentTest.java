package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.messages.probe.HeartbeatAgent;
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

public class HeartbeatAgentTest {

    private final File basePath;

    private final Count count;

    public HeartbeatAgentTest() throws IOException {
        File tempFile = File.createTempFile("heartbeatAgentTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        basePath = new File(tmpFolder, UUID.randomUUID().toString());
        basePath.mkdir();

        tempFile.deleteOnExit();
        basePath.deleteOnExit();

        count = new Count();
    }

    @Test
    public void testHeartbeat() throws BaseEngineCreationException, InterruptedException {
        Engine engine = ProcessManager.getInstance().createEngine("testHeartbeat", basePath);
        try {
            HeartbeatAgent agent = new HeartbeatAgent("myChannel", "BEAT", 10);

            engine.plugChannel(new MyHeartbeatChannel((message, engineUuid) -> count.count++));
            engine.activateChannels();

            Assertions.assertThat(count.count).isEqualTo(0);

            agent.activate(engine.getUuid());

            sleep(10);
            int tmpCount = count.count;
            Assertions.assertThat(tmpCount).isBetween(0, 1);

            sleep(10);
            int tmpCount2 = count.count;
            Assertions.assertThat(tmpCount2).isBetween(1, 2);

            sleep(30);
            int tmpCount3 = count.count;
            Assertions.assertThat(tmpCount3).isBetween(4, 5);
        } finally {
            engine.shutdown();
        }
    }

    // Utilities and classes

    private void sleep(int ms) throws InterruptedException {
        new CountDownLatch(1).await(ms, TimeUnit.MILLISECONDS);
    }

    private class MyHeartbeatChannel extends ProcessingChannel {

        public MyHeartbeatChannel(Agent agent) {
            super("myChannel", 1, agent);
        }
    }

    private class Count {
        int count;
    }
}