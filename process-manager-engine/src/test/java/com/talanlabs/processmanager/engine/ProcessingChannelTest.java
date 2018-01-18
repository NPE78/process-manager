package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.exceptions.NotStartedChannelException;
import org.junit.Test;

public class ProcessingChannelTest {

    @Test(expected = NotStartedChannelException.class)
    public void testHeartbeat() throws BaseEngineCreationException {
        Engine engine = PM.get().createEngine("testHeartbeat", TestUtils.getErrorPath());
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
