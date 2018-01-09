package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.exceptions.NotStartedChannelException;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.junit.Test;

public class ProcessingChannelTest {

    private final File basePath;

    public ProcessingChannelTest() throws IOException {
        File tempFile = File.createTempFile("processingChannelTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        basePath = new File(tmpFolder, UUID.randomUUID().toString());
        basePath.mkdir();

        tempFile.deleteOnExit();
        basePath.deleteOnExit();
    }

    @Test(expected = NotStartedChannelException.class)
    public void testHeartbeat() throws BaseEngineCreationException {
        Engine engine = ProcessManager.getInstance().createEngine("testHeartbeat", basePath);
        try {
            Agent agent = (message, engineUuid) -> {
            };

            MyChannel channel = new MyChannel(agent);
            engine.plugChannel(channel);

            channel.shutdown();
        } finally {
            engine.shutdown();
        }
    }

    private class MyChannel extends ProcessingChannel {

        public MyChannel(Agent agent) {
            super("myChannel", 1, agent);
        }
    }
}
