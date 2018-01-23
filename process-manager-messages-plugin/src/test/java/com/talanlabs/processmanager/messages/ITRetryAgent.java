package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.messages.agent.RetryAgent;
import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class ITRetryAgent {

    private final LogService logService;

    private final File errorPath;

    public ITRetryAgent() throws IOException {
        logService = LogManager.getLogService(getClass());

        File tempFile = File.createTempFile("retryAgentTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        errorPath = new File(tmpFolder, UUID.randomUUID().toString());
        errorPath.mkdir();

        tempFile.deleteOnExit();
        errorPath.deleteOnExit();
    }

    @Test
    public void testRetryAgent() throws BaseEngineCreationException, InterruptedException {
        Engine engine = PM.get().createEngine("testRetry", errorPath);
        try {
            engine.plugChannel(new MyChannel());
            engine.plugChannel(new MyRetryChannel());

            engine.activateChannels();

            Assertions.assertThat(engine.isAvailable("channel")).isTrue();
            Assertions.assertThat(engine.isAvailable("retryChannel")).isTrue();

            MyFlux flux = new MyFlux();
            Assertions.assertThat(flux.getRetryNumber()).isEqualTo(0);
            engine.handle("channel", flux);

            TestUtils.sleep(1000);

            Assertions.assertThat(flux.getRetryNumber()).isGreaterThanOrEqualTo(6);
        } finally {
            engine.shutdown();
        }
    }

    // Utilities and classes

    private class MyChannel extends ProcessingChannel {

        MyChannel() {
            super("channel", 5, new MyExceptionAgent());
        }
    }

    private class MyRetryChannel extends ProcessingChannel {

        MyRetryChannel() {
            super("retryChannel", 5, new RetryAgent("testRetry"));
        }
    }

    private class MyExceptionAgent implements Agent {

        @Override
        public void work(Serializable message) {
            logService.info(() -> "Received message of type " + message.getClass());
            PM.getEngine("testRetry").handle("retryChannel", message);
        }
    }

    private class MyFlux extends AbstractImportFlux {

        MyFlux() {
            setName("channel");
        }
    }
}
