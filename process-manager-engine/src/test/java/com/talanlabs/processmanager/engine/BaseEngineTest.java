package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.engine.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class BaseEngineTest {

    private final LogService logService;
    private final File errorPath;

    private CountDownLatch countDownLatch;

    public BaseEngineTest() throws IOException {
        logService = LogManager.getLogService(BaseEngineTest.class);

        File tmpFolder = File.createTempFile("test", "tmp").getParentFile();
        errorPath = new File(tmpFolder, UUID.randomUUID().toString());
        errorPath.mkdir();
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

            TestChannel channel = new TestChannel(channelName);
            engine.plugChannel(channel);

            Assertions.assertThat(channel.isLocal()).isTrue();
            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.isBusy(channelName)).isFalse();
            Assertions.assertThat(engine.isOverloaded(channelName)).isFalse();
            Assertions.assertThat(channel.getNbWorking()).isEqualTo(0);

            engine.activateChannels();

            sleep(500);

            Assertions.assertThat(engine.isAvailable(channelName)).isTrue();
            Assertions.assertThat(engine.isBusy(channelName)).isTrue();
            Assertions.assertThat(channel.getNbWorking()).isEqualTo(1);

            countDownLatch.countDown();

            sleep(500);

            Assertions.assertThat(engine.isBusy(channelName)).isFalse();

            channel.setAvailable(false);

            engine.handle(channelName, "second message");

            Assertions.assertThat(engine.isBusy(channelName)).isFalse();

            engine.unplugChannel(channelName);
            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();

            channel.shutdown();
        } finally {
            engine.shutdown();
        }

        File[] listFiles = errorPath.listFiles();
        Assertions.assertThat(listFiles).isNotNull();
        Assertions.assertThat(listFiles.length).isEqualTo(1);
    }

    @Test
    public void testAgentException() throws BaseEngineCreationException, InterruptedException {
        Engine engine = ProcessManager.getInstance().createEngine("test", errorPath);
        try {
            String channelName = "exception";

            countDownLatch = new CountDownLatch(1);

            TestChannel channel = new TestChannel(channelName);
            engine.plugChannel(channel);

            Assertions.assertThat(channel.isLocal()).isTrue();
            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.isBusy(channelName)).isFalse();
            Assertions.assertThat(engine.isOverloaded(channelName)).isFalse();

            engine.activateChannels();

            Assertions.assertThat(engine.isAvailable(channelName)).isTrue();
            Assertions.assertThat(engine.isBusy(channelName)).isFalse();
            Assertions.assertThat(engine.isOverloaded(channelName)).isFalse();

            engine.handle(channelName, "exception message");

            sleep(500);

            Assertions.assertThat(engine.isBusy(channelName)).isTrue();

            countDownLatch.countDown();

            sleep(500);

            Assertions.assertThat(engine.isAvailable(channelName)).isTrue();
            Assertions.assertThat(engine.isBusy(channelName)).isFalse();

        } finally {
            ProcessManager.getInstance().shutdownEngine("test");
        }
    }

    @Test(expected = BaseEngineCreationException.class)
    public void testCreatedTwice() throws BaseEngineCreationException {
        ProcessManager.getInstance().createEngine("test", errorPath);
        Assertions.assertThat(ProcessManager.getInstance().getEngine("test")).isNotNull();
        try {
            ProcessManager.getInstance().createEngine("test", errorPath);
        } finally {
            ProcessManager.getInstance().shutdownEngine("test");

            Assertions.assertThat(ProcessManager.getInstance().getEngine("test")).isNull();
        }
    }

    // Utilities and classes

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
                if ("exception message".equals(message)) {
                    throw new RuntimeException();
                }
                logService.info(() -> "TestAgent is working");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
