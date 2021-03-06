package com.talanlabs.processmanager.rest;

import com.jayway.restassured.RestAssured;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.rest.agent.AbstractRestAgent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import io.javalin.Context;
import org.assertj.core.api.Assertions;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.Serializable;

public class AgentRestTimeoutTest {

    @Test
    public void testTimeout() throws BaseEngineCreationException {
        Engine engine = PM.createEngine("rest", TestUtils.getErrorPath());
        try {
            RestAddon restAddon = RestAddon.register("rest");
            restAddon.start(8080);

            MyRestDispatcher restDispatcher = new MyRestDispatcher();
            MyRestAgent agentGet = new MyRestAgent();
            restDispatcher.setAgentGet(agentGet);
            Assertions.assertThat(restDispatcher.getTimeout()).isEqualTo(120L * 1000L);

            restAddon.bindDispatcher(restDispatcher);

            agentGet.register("rest", 1);
            engine.activateChannels();

            RestAssured.given().when().get("http://localhost:8080/rest?hello=hi").then().statusCode(HttpStatus.OK_200);

            long newTimeout = 100L;
            restDispatcher.setTimeout(newTimeout);
            Assertions.assertThat(restDispatcher.getTimeout()).isEqualTo(newTimeout);
            RestAssured.given().when().get("http://localhost:8080/rest?hello=hi").then().statusCode(HttpStatus.REQUEST_TIMEOUT_408);
        } finally {
            engine.shutdown();
        }
    }

    private class MyRestDispatcher extends AbstractRestDispatcher {

        MyRestDispatcher() {
            super("rest");
        }
    }

    private class MyRestAgent extends AbstractRestAgent {

        MyRestAgent() {
            super("restAgent");
        }

        @Override
        public int getMaxWaiting() {
            return 0;
        }

        @Override
        public Serializable extract(Context context) {
            return null;
        }

        @Override
        protected void doWork(Serializable message, Context context) {
            try {
                TestUtils.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
