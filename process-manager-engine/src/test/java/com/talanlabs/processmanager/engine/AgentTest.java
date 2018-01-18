package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.AgentException;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import java.io.Serializable;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AgentTest {

    @Test(expected = AgentException.class)
    public void testAgentExceptionNoEngine() {
        MyAgent agent = new MyAgent();
        agent.register(null, 1);
    }

    @Test(expected = AgentException.class)
    public void testAgentExceptionInvalidMaxWorking() {
        MyAgent agent = new MyAgent();
        agent.register("exception", 0);
    }

    @Test(expected = AgentException.class)
    public void testAgentExceptionUnknownEngine() {
        MyAgent agent = new MyAgent();
        agent.register("unknownEngine", 1);
    }

    @Test(expected = AgentException.class)
    public void testAgentExceptionBoundTwice() throws BaseEngineCreationException {
        PM.get().createEngine("testAgent", TestUtils.getErrorPath());
        try {
            MyAgent agent = new MyAgent();
            agent.register("testAgent", 1);
            agent.register("testAgent2", 1);
        } finally {
            PM.get().shutdownEngine("testAgent");
        }
    }

    @Test
    public void testAgentValid() throws BaseEngineCreationException {
        Engine engine = PM.get().createEngine("testAgent", TestUtils.getErrorPath());
        try {
            Assertions.assertThat(engine.getPluggedChannels()).isEmpty();

            MyAgent agent = new MyAgent();
            agent.register("testAgent", 1);

            Assertions.assertThat(engine.getPluggedChannels()).hasSize(1);
            engine.activateChannels();

            agent.unregister();
            Assertions.assertThat(engine.getPluggedChannels()).isEmpty();
        } finally {
            PM.get().shutdownEngine("testAgent");
        }
    }

    private class MyAgent extends AbstractAgent {

        public MyAgent() {
            super("myAwesomeName");
        }

        @Override
        public void work(Serializable message, String engineUuid) {

        }
    }
}
