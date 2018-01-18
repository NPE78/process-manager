package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.AddonAlreadyBoundException;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class BaseEngineTest {

    private final LogService logService;

    private CountDownLatch countDownLatch;

    public BaseEngineTest() {
        logService = LogManager.getLogService(getClass());
    }

    @Test
    public void mainTest() throws BaseEngineCreationException, InterruptedException {

        Engine engine = PM.get().createEngine("test", TestUtils.getErrorPath());
        try {
            String channelName = "channel";
            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.getUuid()).isEqualTo("test");
            Assertions.assertThat(engine.getChannelSlots()).isEmpty();

            engine.handle(channelName, "test message");

            Assertions.assertThat(engine.getChannelSlots()).hasSize(1);
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

            TestUtils.sleep(500);

            Assertions.assertThat(engine.isAvailable(channelName)).isTrue();
            Assertions.assertThat(engine.isBusy(channelName)).isTrue();
            Assertions.assertThat(engine.getNbWorking(channelName)).isEqualTo(1);

            countDownLatch.countDown();

            TestUtils.sleep(500);

            Assertions.assertThat(engine.isBusy(channelName)).isFalse();

            engine.setAvailable(channelName, false);

            engine.handle(channelName, "second message");

            Assertions.assertThat(engine.isBusy(channelName)).isFalse();

            Assertions.assertThat(engine.getChannelSlots()).hasSize(1);
            engine.unplugChannel(channelName);
            Assertions.assertThat(engine.getChannelSlots()).hasSize(1);
            Assertions.assertThat(engine.isAvailable(channelName)).isFalse();
            Assertions.assertThat(engine.getPluggedChannels()).isEmpty();
        } finally {
            engine.shutdown();
        }

        File[] listFiles = TestUtils.getErrorPath().listFiles();
        Assertions.assertThat(listFiles).isNotNull();
        Assertions.assertThat(listFiles.length).isEqualTo(1);
    }

    @Test
    public void testAgentException() throws BaseEngineCreationException, InterruptedException {
        Engine engine = PM.get().createEngine("test", TestUtils.getErrorPath());
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

            TestUtils.sleep(500);

            Assertions.assertThat(engine.isBusy(channelName)).isTrue();

            countDownLatch.countDown();

            TestUtils.sleep(500);

            Assertions.assertThat(engine.isAvailable(channelName)).isTrue();
            Assertions.assertThat(engine.isBusy(channelName)).isFalse();

        } finally {
            PM.get().shutdownEngine("test");
        }
    }

    @Test(expected = BaseEngineCreationException.class)
    public void testCreatedTwice() throws BaseEngineCreationException {
        PM.get().createEngine("test", TestUtils.getErrorPath());
        Assertions.assertThat(PM.getEngine("test").toString()).isEqualTo("Base Engine test");
        Assertions.assertThat(PM.getEngine("test")).isNotNull();
        try {
            PM.get().createEngine("test", TestUtils.getErrorPath());
        } finally {
            PM.get().shutdownEngine("test");

            Assertions.assertThat(PM.getEngine("test")).isNull();
        }
    }

    @Test
    public void testProperties() throws BaseEngineCreationException, AddonAlreadyBoundException {
        Engine engine = PM.get().createEngine("test", TestUtils.getErrorPath());
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
        Engine engine = PM.get().createEngine("test", TestUtils.getErrorPath());
        try {
            createAddon(engine);
            engine.addAddon(new MyEngineAddon("test"));
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
