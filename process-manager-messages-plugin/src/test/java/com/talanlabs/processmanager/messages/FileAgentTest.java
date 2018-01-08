package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.messages.agent.AbstractFileAgent;
import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.messages.gate.GateFactory;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class FileAgentTest {

    private final LogService logService;

    private final File basePath;

    public FileAgentTest() throws IOException {
        logService = LogManager.getLogService(getClass());

        File tempFile = File.createTempFile("fileAgentTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        basePath = new File(tmpFolder, UUID.randomUUID().toString());
        basePath.mkdir();

        tempFile.deleteOnExit();
        basePath.deleteOnExit();
    }

    @Test
    public void testFileAgent() throws BaseEngineCreationException, IOException {
        Engine engine = ProcessManager.getInstance().createEngine("testFileAgent", basePath);
        try {
            MyAgent myAgent = new MyAgent();

            GateFactory.register("testFileAgent");

            File expectedFile = new File(basePath, "MyFlux");
            Assertions.assertThat(expectedFile).doesNotExist();
            myAgent.register("testFileAgent", 5, 200, basePath);
            Assertions.assertThat(expectedFile).exists();

            File testFlux = new File(expectedFile, "testFile");
            boolean created = testFlux.createNewFile();
            Assertions.assertThat(created).isTrue();
            Assertions.assertThat(testFlux).exists();

            engine.activateChannels();
            sleep(300);

            Assertions.assertThat(testFlux).doesNotExist();
            Assertions.assertThat(new File(testFlux.getParentFile(), "accepted/testFile")).exists();
        } catch (Exception e ) {
            logService.error(() -> "error", e);
        } finally {
            engine.shutdown();
        }
    }

    // Utilities and classes

    private void sleep(int ms) throws InterruptedException {
        new CountDownLatch(1).await(ms, TimeUnit.MILLISECONDS);
    }

    private class MyFlux extends AbstractImportFlux {

    }

    private class MyAgent extends AbstractFileAgent<MyFlux> {

        MyAgent() {
            super(MyFlux.class);
        }

        @Override
        public void doWork(MyFlux flux, String engineUuid) {
            File file = flux.getFile();
            boolean renamed = file.renameTo(new File(file.getParentFile(), "accepted/" + file.getName()));
            Assertions.assertThat(renamed).isTrue();
        }

        @Override
        protected MyFlux createFlux() {
            return new MyFlux();
        }
    }
}
