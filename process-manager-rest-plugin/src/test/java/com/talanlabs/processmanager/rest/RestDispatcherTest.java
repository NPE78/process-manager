package com.talanlabs.processmanager.rest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.rest.agent.AbstractRestAgent;
import com.talanlabs.processmanager.rest.agent.IRestAgent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import io.javalin.Context;
import org.assertj.core.api.Assertions;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RestDispatcherTest {

    @Test
    public void testDispatcherAgent() throws BaseEngineCreationException, InterruptedException {
        Engine engine = PM.get().createEngine("rest", TestUtils.getErrorPath());
        List<String> failures = new ArrayList<>(3);
        CountDownLatch cdlToConsume = new CountDownLatch(3);
        CountDownLatch cdlStart = new CountDownLatch(1);
        CountDownLatch cdlEnd = new CountDownLatch(3);
        try {
            MyRestDispatcher myRestAgent = new MyRestDispatcher();

            RestAddon restAddon = RestAddon.register("rest");
            restAddon.start(8080);

            restAddon.bindDispatcher(myRestAgent);

            myRestAgent.register();

            engine.activateChannels();

            new Thread(() -> threadRun(failures, cdlStart, cdlToConsume, cdlEnd)).start();
            new Thread(() -> threadRun(failures, cdlStart, cdlToConsume, cdlEnd)).start();
            new Thread(() -> threadRun(failures, cdlStart, cdlToConsume, cdlEnd)).start();

            cdlToConsume.await(); // waiting for the 3 threads to be ready
            cdlStart.countDown();
        } finally {
            cdlEnd.await();

            engine.shutdown();
            Assertions.assertThat(failures).hasSize(1);
        }
    }

    private void threadRun(List<String> failures, CountDownLatch cdlStart, CountDownLatch cdlToConsume, CountDownLatch cdlEnd) {
        try {
            test(failures, cdlStart, cdlToConsume);
        } finally {
            cdlEnd.countDown();
        }
    }

    private void test(List<String> failures, CountDownLatch cdlStart, CountDownLatch cdlToConsume) {
        cdlToConsume.countDown();
        waitFor(cdlStart);
        Response response = RestAssured.given().when().get("/rest?param=test");
        try {
            response.then().statusCode(HttpStatus.OK_200);
        } catch (AssertionError e) {
            failures.add(e.getMessage());
        }
    }

    @Test
    public void testNotInitialized() throws BaseEngineCreationException, InterruptedException {
        Engine engine = PM.get().createEngine("rest", TestUtils.getErrorPath());
        try {
            MyRestDispatcher myRestDispatcher = new MyRestDispatcher();

            RestAddon restAddon = RestAddon.register("rest");
            restAddon.start(8080);

            restAddon.bindDispatcher(myRestDispatcher);

            myRestDispatcher.register();

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

            restAddon.bindDispatcher(myRestDispatcher);

            myRestDispatcher.register();

            myRestDispatcher.clear();

            RestAssured.given().when().post("http://localhost:8080/rest?hello=hi").then().statusCode(HttpStatus.SERVICE_UNAVAILABLE_503);
        } finally {
            TestUtils.sleep(200);
            engine.shutdown();
        }
    }

    private void waitFor(CountDownLatch cdl) {

        try {
            cdl.await();
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
            return 1000L;
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
                TestUtils.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            context.result(String.valueOf(message));
        }
    }
}
