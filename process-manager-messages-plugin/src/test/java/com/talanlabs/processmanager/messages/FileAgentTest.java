package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.messages.agent.AbstractFileAgent;
import com.talanlabs.processmanager.messages.exceptions.InjectorNotCreatedYetException;
import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.messages.gate.GateFactory;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class FileAgentTest {

    private final File basePath;

    public FileAgentTest() throws IOException {
        File tempFile = File.createTempFile("fileAgentTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        basePath = new File(tmpFolder, UUID.randomUUID().toString());
        basePath.mkdir();

        tempFile.deleteOnExit();
        basePath.deleteOnExit();
    }

    @Test
    public void testFileAgent() throws BaseEngineCreationException, InterruptedException, IOException {
        Engine engine = ProcessManager.getInstance().createEngine("testFileAgent", basePath);
        try {
            MyAgent myAgent = new MyAgent();
            myAgent.getLogService().info(() -> "Testing myAgent");

            GateFactory.register("testFileAgent");

            File expectedFile = new File(basePath, "MyFlux");
            Assertions.assertThat(expectedFile).doesNotExist();
            myAgent.register("testFileAgent", 5, 200, basePath);
            Assertions.assertThat(expectedFile).exists();

            Assertions.assertThat(myAgent.getWorkDir()).exists();
            Assertions.assertThat(new File(myAgent.getAcceptedPath())).exists();
            Assertions.assertThat(new File(myAgent.getRejectedPath())).exists();
            Assertions.assertThat(new File(myAgent.getRetryPath())).exists();
            Assertions.assertThat(new File(myAgent.getArchivePath())).exists();

            File testFlux = new File(expectedFile, "testFile");
            boolean created = testFlux.createNewFile();
            Assertions.assertThat(created).isTrue();
            Assertions.assertThat(testFlux).exists();

            engine.activateChannels();
            sleep(300);

            Assertions.assertThat(testFlux).doesNotExist();
            Assertions.assertThat(new File(myAgent.getAcceptedPath(), "testFile")).exists();

            Assertions.assertThat(engine.getPluggedChannels()).hasSize(1);
            myAgent.unregister();
            Assertions.assertThat(engine.getPluggedChannels()).isEmpty();

        } finally {
            engine.shutdown();
        }
    }

    @Test
    public void testFileAgentReject() throws BaseEngineCreationException, IOException, InterruptedException {
        Engine engine = ProcessManager.getInstance().createEngine("testFileAgent", basePath);
        try {
            MyAgent myAgent = new MyAgent();

            myAgent.register("testFileAgent", 5, 200, basePath);

            File testFlux = new File(myAgent.getWorkDir(), "rejectedFile");
            boolean created = testFlux.createNewFile();
            Assertions.assertThat(created).isTrue();
            Assertions.assertThat(testFlux).exists();

            engine.activateChannels();
            sleep(300);

            Assertions.assertThat(testFlux).doesNotExist();
            Assertions.assertThat(new File(myAgent.getRejectedPath(), "rejectedFile")).exists();

            myAgent.work("Invalid msg", null); // does nothing
        } finally {
            engine.shutdown();
        }
    }

    @Test(expected = InjectorNotCreatedYetException.class)
    public void testAgentNotRegistered() throws BaseEngineCreationException {
        Engine engine = ProcessManager.getInstance().createEngine("testFileAgent", basePath);
        try {
            MyAgent myAgent = new MyAgent();
            myAgent.getLogService().info(() -> "Testing myAgent");

            Assertions.assertThat(myAgent.getWorkDir()).doesNotExist();
        } finally {
            engine.shutdown();
        }
    }

    @Test
    public void invalidPathAgent() {
        MyAgent agent = new MyAgent() {
            @Override
            public void init() {
                buildInjector("test", TestUtils.getErrorPath());
            }
        };
        agent.init();

        MyFlux flux = new MyFlux();
        File file = new File(TestUtils.getErrorPath(), "testFile");
        flux.setFile(file);

        Assertions.assertThat(file).doesNotExist();
        agent.acceptFlux(flux);
        Assertions.assertThat(file).doesNotExist();

        Assertions.assertThat(file).doesNotExist();
        agent.rejectFlux(flux);
        Assertions.assertThat(file).doesNotExist();
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
            if ("rejectedFile".equals(flux.getFilename())) {
                rejectFlux(flux);
            } else {
                acceptFlux(flux);
            }
        }

        public void init() {
        }

        @Override
        protected MyFlux createFlux() {
            return new MyFlux();
        }
    }
}
