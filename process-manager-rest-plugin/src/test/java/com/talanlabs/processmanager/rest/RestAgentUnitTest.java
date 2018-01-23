package com.talanlabs.processmanager.rest;

import com.talanlabs.processmanager.rest.agent.AbstractRestAgent;
import com.talanlabs.processmanager.rest.exceptions.RestAgentException;
import com.talanlabs.processmanager.rest.model.LockedMessage;
import com.talanlabs.processmanager.shared.TestUtils;
import io.javalin.Context;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RestAgentUnitTest {

    @Test
    public void testLock() {
        List<String> steps = new ArrayList<>();
        MyRestAgent myRestAgent = new MyRestAgent(steps);
        UUID id = UUID.randomUUID();
        CountDownLatch lock = myRestAgent.lock(id, null);
        Assertions.assertThat(lock.getCount()).isEqualTo(1L);

        myRestAgent.removeLock(id);
        Assertions.assertThat(lock.getCount()).isEqualTo(0L);
        myRestAgent.removeLock(id);
        Assertions.assertThat(lock.getCount()).isEqualTo(0L);
    }

    @Test
    public void testMessage() {
        List<String> steps = new ArrayList<>();
        MyRestAgent myRestAgent = new MyRestAgent(steps);
        UUID id = UUID.randomUUID();
        CountDownLatch lock = myRestAgent.lock(id, null);
        Assertions.assertThat(lock.getCount()).isEqualTo(1L);

        Assertions.assertThat(steps).isEmpty();
        LockedMessage message = new LockedMessage(id, "testMessage");
        myRestAgent.work(message);

        Assertions.assertThat(lock.getCount()).isEqualTo(0L);
        Assertions.assertThat(steps).containsExactly("DO WORK");
    }

    @Test
    public void testMessageExpired() {
        List<String> steps = new ArrayList<>();
        MyRestAgent myRestAgent = new MyRestAgent(steps);
        UUID id = UUID.randomUUID();
        CountDownLatch lock = myRestAgent.lock(id, null);
        Assertions.assertThat(lock.getCount()).isEqualTo(1L);

        myRestAgent.removeLock(id);
        Assertions.assertThat(lock.getCount()).isEqualTo(0L);

        Assertions.assertThat(steps).isEmpty();
        LockedMessage message = new LockedMessage(id, "testMessage");
        myRestAgent.work(message);

        Assertions.assertThat(lock.getCount()).isEqualTo(0L);
        Assertions.assertThat(steps).isEmpty();
    }

    @Test(expected = RestAgentException.class)
    public void testLockException() {
        MyRestAgent myRestAgent = new MyRestAgent(Collections.emptyList());
        UUID id = UUID.randomUUID();
        myRestAgent.lock(id, null);
        myRestAgent.lock(id, null);
    }

    @Test
    public void testLocks() throws InterruptedException {
        test("GET");
        test("POST");
        test("PUT");
        test("PATCH");
        test("DELETE");
        test("default");
    }

    private void test(String method) throws InterruptedException {
        MyRestDispatcher restDispatcher = new MyRestDispatcher();
        CountDownLatch cdlStart = new CountDownLatch(2);
        CountDownLatch cdlToWait = new CountDownLatch(1);
        CountDownLatch cdlShared = new CountDownLatch(1);
        CountDownLatch cdlShared2 = new CountDownLatch(1);
        List<String> errors = new ArrayList<>();
        new Thread(() -> testLock(cdlStart, cdlToWait, cdlShared, cdlShared2, true, errors, restDispatcher.getLock(method))).start();
        new Thread(() -> testLock(cdlStart, cdlToWait, cdlShared, cdlShared2, false, errors, restDispatcher.getLock(method))).start();

        cdlStart.await();
        cdlToWait.countDown();

        cdlShared.await();
        TestUtils.sleep(100);

        Assertions.assertThat(errors).isEmpty();
    }

    private void testLock(CountDownLatch cdlStart, CountDownLatch cdlToWait, CountDownLatch cdlShared, CountDownLatch cdlShared2,
                          boolean writeMode, List<String> errors, Object method) {
        cdlStart.countDown();
        try {
            cdlToWait.await();
            if (writeMode) {
                acquireLock(cdlShared, cdlShared2, method);
            } else {
                waitLock(cdlShared, cdlShared2, method);
            }
        } catch (Throwable e) {
            errors.add("BUG");
        }
    }

    private void waitLock(CountDownLatch cdlShared, CountDownLatch cdlShared2, Object method) throws InterruptedException {
        cdlShared.await();
        synchronized (method) {
            System.out.println("acquire sync for wait");
            System.out.println("release sync for wait");
            cdlShared2.countDown();
        }
    }

    private void acquireLock(CountDownLatch cdlShared, CountDownLatch cdlShared2, Object method) throws InterruptedException {
        synchronized (method) {
            System.out.println("acquire sync to write");
            cdlShared.countDown();
            Assertions.assertThat(cdlShared2.await(50, TimeUnit.MILLISECONDS)).isFalse();
            System.out.println("release sync to write");
        }
    }

    private class MyRestDispatcher extends AbstractRestDispatcher {

        MyRestDispatcher() {
            super("rest");
        }
    }

    private class MyRestAgent extends AbstractRestAgent {

        private final List<String> steps;

        MyRestAgent(List<String> steps) {
            super("restAgent");

            this.steps = steps;
        }

        @Override
        protected void doWork(Serializable message, Context context) {
            steps.add("DO WORK");
        }

        @Override
        public int getMaxWaiting() {
            return 0;
        }

        @Override
        public Serializable extract(Context context) {
            return null;
        }
    }
}
