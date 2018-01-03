package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.engine.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class BaseEngineTest {

    private final LogService logService;
    private final File errorPath;

    private CountDownLatch countDownLatch;
    private CountDownLatch agentCountDownLatch;

    public BaseEngineTest() throws IOException {
        logService = LogManager.getLogService(BaseEngineTest.class);

        errorPath = File.createTempFile("test", "tmp").getParentFile();
    }

    @Test
    public void mainTest() throws BaseEngineCreationException, InterruptedException {

        Engine engine = ProcessManager.getInstance().createEngine("test", errorPath);

        try {
            String channelName = "channel";
            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();

            engine.handle(channelName, "test message");

            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.isBusy(channelName)).isFalse();
            Assertions.assertThat(engine.isOverloaded(channelName)).isFalse();

            countDownLatch = new CountDownLatch(1);
            agentCountDownLatch = new CountDownLatch(1);

            engine.plugChannel(new TestChannel(channelName));

            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.isBusy(channelName)).isFalse();
            Assertions.assertThat(engine.isOverloaded(channelName)).isFalse();

            engine.activateChannels();

            sleep(500);

            Assertions.assertThat(engine.isAvailable(channelName)).isTrue();
            Assertions.assertThat(engine.isBusy(channelName)).isTrue();

            countDownLatch.countDown();

            sleep(500);

            Assertions.assertThat(engine.isBusy(channelName)).isFalse();
        } finally {
            engine.shutdown();
        }
    }

    private void sleep(int ms) throws InterruptedException {
        new CountDownLatch(1).await(ms, TimeUnit.MILLISECONDS);
    }

    private class TestChannel extends ProcessingChannel {

        TestChannel(String channelName) {
            super(channelName, 5, new TestAgent());
        }
    }

    private class TestAgent implements Agent {

        @Override
        public void work(Serializable message) {
            try {
                logService.info(() -> "TestAgent is waiting for the countDownLatch to be consumed");
                countDownLatch.await();
                logService.info(() -> "TestAgent is working");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
