package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.exceptions.AddonAlreadyBoundException;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
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
        logService = LogManager.getLogService(getClass());

        File tempFile = File.createTempFile("baseEngineTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        errorPath = new File(tmpFolder, UUID.randomUUID().toString());
        errorPath.mkdir();

        tempFile.deleteOnExit();
        errorPath.deleteOnExit();
    }

    @Test
    public void mainTest() throws BaseEngineCreationException, InterruptedException {

        Engine engine = ProcessManager.getInstance().createEngine("test", errorPath);
        try {
            String channelName = "channel";
            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.getUuid()).isEqualTo("test");

            engine.handle(channelName, "test message");

            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.isBusy(channelName)).isFalse();
            Assertions.assertThat(engine.isOverloaded(channelName)).isFalse();
            Assertions.assertThat(engine.getPluggedChannels()).isEmpty();

            countDownLatch = new CountDownLatch(1);

            TestChannel channel = new TestChannel(channelName);
            engine.plugChannel(channel);

            Assertions.assertThat(channel.isLocal()).isTrue();
            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.isBusy(channelName)).isFalse();
            Assertions.assertThat(engine.isOverloaded(channelName)).isFalse();
            Assertions.assertThat(engine.getNbWorking(channelName)).isEqualTo(0);
            Assertions.assertThat(engine.getPluggedChannels()).hasSize(1);
            Assertions.assertThat(channel.getAgent().getClass().getSimpleName()).isEqualTo("TestAgent");

            engine.activateChannels();

            sleep(500);

            Assertions.assertThat(engine.isAvailable(channelName)).isTrue();
            Assertions.assertThat(engine.isBusy(channelName)).isTrue();
            Assertions.assertThat(engine.getNbWorking(channelName)).isEqualTo(1);

            countDownLatch.countDown();

            sleep(500);

            Assertions.assertThat(engine.isBusy(channelName)).isFalse();

            engine.setAvailable(channelName, false);

            engine.handle(channelName, "second message");

            Assertions.assertThat(engine.isBusy(channelName)).isFalse();

            engine.unplugChannel(channelName);
            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.getPluggedChannels()).isEmpty();

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
        Assertions.assertThat(ProcessManager.getEngine("test").toString()).isEqualTo("Base Engine test");
        Assertions.assertThat(ProcessManager.getEngine("test")).isNotNull();
        try {
            ProcessManager.getInstance().createEngine("test", errorPath);
        } finally {
            ProcessManager.getInstance().shutdownEngine("test");

            Assertions.assertThat(ProcessManager.getEngine("test")).isNull();
        }
    }

    @Test
    public void testProperties() throws BaseEngineCreationException, AddonAlreadyBoundException {
        Engine engine = ProcessManager.getInstance().createEngine("test", errorPath);
        try {
            MyEngineAddon myEngineAddon = createAddon(engine);
            Assertions.assertThat(engine.getAddon(myEngineAddon.getAddonClass()).isPresent()).isTrue();
            Assertions.assertThat(engine.getAddon(myEngineAddon.getAddonClass()).get()).isEqualTo(myEngineAddon);
        } finally {
            engine.shutdown();
        }
    }

    @Test(expected = AddonAlreadyBoundException.class)
    public void testPropertiesAlreadyBind() throws BaseEngineCreationException, AddonAlreadyBoundException {
        Engine engine = ProcessManager.getInstance().createEngine("test", errorPath);
        try {
            MyEngineAddon myEngineAddon = createAddon(engine);
            engine.addAddon(myEngineAddon);
        } finally {
            engine.shutdown();
        }
    }

    private MyEngineAddon createAddon(Engine engine) throws AddonAlreadyBoundException {
        MyEngineAddon myEngineAddon = new MyEngineAddon(engine.getUuid());
        Assertions.assertThat(engine.getAddon(myEngineAddon.getAddonClass()).isPresent()).isFalse();
        engine.addAddon(myEngineAddon);
        return myEngineAddon;
    }

    // Utilities and classes

    private void sleep(int ms) throws InterruptedException {
        new CountDownLatch(1).await(ms, TimeUnit.MILLISECONDS);
    }

    private class MyEngineAddon extends EngineAddon<MyEngineAddon> {

        MyEngineAddon(String engineUuid) {
            super(MyEngineAddon.class, engineUuid);
        }

        @Override
        public void disconnectAddon() {

        }
    }

    private class TestChannel extends ProcessingChannel {

        TestChannel(String channelName) {
            super(channelName, 5, new TestAgent());
        }
    }

    private class TestAgent implements Agent {

        @Override
        public void work(Serializable message, String engineUuid) {
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
