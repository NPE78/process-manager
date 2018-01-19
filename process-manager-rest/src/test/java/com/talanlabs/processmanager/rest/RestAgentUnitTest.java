package com.talanlabs.processmanager.rest;

import com.talanlabs.processmanager.shared.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RestAgentUnitTest {

    @Test
    public void testLocks() throws InterruptedException {
        test("GET");
        test("POST");
        test("PUT");
        test("PATCH");
        test("DELETE");
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
                          boolean writeMode, List<String> errors, Object get) {
        cdlStart.countDown();
        try {
            cdlToWait.await();
            if (writeMode) {
                acquireLock(cdlShared, cdlShared2, get);
            } else {
                waitLock(cdlShared, cdlShared2, get);
            }
        } catch (Throwable e) {
            errors.add("BUG");
        }
    }

    private void waitLock(CountDownLatch cdlShared, CountDownLatch cdlShared2, Object get) throws InterruptedException {
        cdlShared.await();
        synchronized (get) {
            System.out.println("acquire sync for wait");
            System.out.println("release sync for wait");
            cdlShared2.countDown();
        }
    }

    private void acquireLock(CountDownLatch cdlShared, CountDownLatch cdlShared2, Object get) throws InterruptedException {
        synchronized (get) {
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
}
