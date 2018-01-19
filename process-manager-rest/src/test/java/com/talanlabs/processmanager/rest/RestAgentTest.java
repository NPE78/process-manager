package com.talanlabs.processmanager.rest;

import com.jayway.restassured.RestAssured;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.rest.agent.AbstractRestAgent;
import com.talanlabs.processmanager.rest.agent.IRestAgent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import io.javalin.Context;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

public class RestAgentTest {

    @Test
    public void testRestAgent() throws BaseEngineCreationException, InterruptedException {
        Engine engine = PM.get().createEngine("rest", TestUtils.getErrorPath());
        try {
            MyRestDispatcher myRestAgent = new MyRestDispatcher();

            RestAddon restAddon = RestAddon.register("rest");
            restAddon.start(8080);

            restAddon.bindAgent(myRestAgent);

            myRestAgent.register();

            engine.activateChannels();

            CountDownLatch cdl = new CountDownLatch(3);
            CountDownLatch cdl2 = new CountDownLatch(1);
            new Thread(() -> {
                cdl.countDown();
                waitFor(cdl2, 0);
                RestAssured.given().when().get("http://localhost:8080/rest?hello=hi").then().statusCode(200);
            }).start();
            new Thread(() -> {
                cdl.countDown();
                waitFor(cdl2, 100);
                RestAssured.given().when().get("http://localhost:8080/rest?hello=hi").then().statusCode(200);
            }).start();
            new Thread(() -> {
                cdl.countDown();
                waitFor(cdl2, 200);
                RestAssured.given().when().get("http://localhost:8080/rest?hello=hi").then().statusCode(HttpStatus.TOO_MANY_REQUESTS_429);
            }).start();

            cdl.await(); // waiting for the 3 threads to be ready
            cdl2.countDown();

        } finally {
            TestUtils.sleep(2000);
            engine.shutdown();
        }
    }

    @Test
    public void testNotInitialized() throws BaseEngineCreationException, InterruptedException {
        Engine engine = PM.get().createEngine("rest", TestUtils.getErrorPath());
        try {
            MyRestDispatcher myRestAgent = new MyRestDispatcher();

            RestAddon restAddon = RestAddon.register("rest");
            restAddon.start(8080);

            restAddon.bindAgent(myRestAgent);

            myRestAgent.register();

            RestAssured.given().when().post("http://localhost:8080/rest?hello=hi").then().statusCode(HttpStatus.METHOD_NOT_ALLOWED_405);
        } finally {
            TestUtils.sleep(200);
            engine.shutdown();
        }
    }

    @Test
    public void testDeactivated() throws BaseEngineCreationException, InterruptedException {
        Engine engine = PM.get().createEngine("rest", TestUtils.getErrorPath());
        try {
            MyRestDispatcher myRestDispatcher = new MyRestDispatcher();

            RestAddon restAddon = RestAddon.register("rest");
            restAddon.start(8080);

            restAddon.bindAgent(myRestDispatcher);

            myRestDispatcher.register();

            myRestDispatcher.clear();

            RestAssured.given().when().post("http://localhost:8080/rest?hello=hi").then().statusCode(HttpStatus.SERVICE_UNAVAILABLE_503);
        } finally {
            TestUtils.sleep(200);
            engine.shutdown();
        }
    }

    private void waitFor(CountDownLatch cdl, int i) {

        try {
            cdl.await();
            TestUtils.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class MyRestDispatcher extends AbstractRestDispatcher {

        private final MyAgentGet agentGet;

        MyRestDispatcher() {
            super("rest");

            agentGet = new MyAgentGet();
        }

        @Override
        protected long getTimeout() {
            return 10L;
        }

        @Override
        protected IRestAgent agentGet() {
            return agentGet;
        }

        void register() {
            agentGet.register("rest", 1);
        }
    }

    private class MyAgentGet extends AbstractRestAgent {

        MyAgentGet() {
            super("restAgent");
        }

        @Override
        public int getMaxWaiting() {
            return 1;
        }

        @Override
        public Serializable extract(Context context) {
            return context.queryString();
        }

        @Override
        protected void doWork(Serializable message, Context context) {
            try {
                TestUtils.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            context.result(String.valueOf(message));
        }
    }
}
