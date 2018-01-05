package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.messages.helper.IncrementHelper;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class IncrementHelperTest {

    private final LogService logService;

    public IncrementHelperTest() {
        logService = LogManager.getLogService(getClass());
    }

    @Test
    public void testIncrementHelper() throws InterruptedException {
        int m = 500;
        Set<String> increments = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch cdl = new CountDownLatch(1);
        CountDownLatch cdl2 = new CountDownLatch(m);
        CountDownLatch cdl3 = new CountDownLatch(m);
        for (int i = 0; i < m; i++) {
            new MyThread(cdl, cdl2, cdl3, increments).start();
        }
        boolean await = cdl2.await(6, TimeUnit.SECONDS);
        Assertions.assertThat(await).isTrue();

        cdl.countDown();
        logService.info(() -> "GO!");

        cdl3.await();

        Assertions.assertThat(increments).hasSize(m);
    }

    private class MyThread extends Thread {

        private final CountDownLatch cdl;
        private final CountDownLatch cdl2;
        private final CountDownLatch cdl3;
        private final Set<String> increments;

        private MyThread(CountDownLatch cdl, CountDownLatch cdl2, CountDownLatch cdl3, Set<String> increments) {
            this.cdl = cdl;
            this.cdl2 = cdl2;
            this.cdl3 = cdl3;
            this.increments = increments;
        }

        @Override
        public void run() {
            try {
                cdl2.countDown();
                cdl.await();

                String uniqueDate = IncrementHelper.getInstance().getUniqueDate();
                boolean added = increments.add(uniqueDate);
                try {
                    Assertions.assertThat(added).isTrue();
                } catch (AssertionError e) {
                    logService.info(() -> "{0} has already been added", uniqueDate);
                    throw e;
                }
                cdl3.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
