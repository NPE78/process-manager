package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.messages.agent.AbstractImportAgent;
import com.talanlabs.processmanager.messages.exceptions.InjectorNotCreatedYetException;
import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.messages.gate.GateFactory;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import java.io.File;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ImportAgentTest {

    @Test
    public void testFileAgent() throws BaseEngineCreationException, InterruptedException, IOException {
        Engine engine = PM.get().createEngine("testFileAgent", TestUtils.getErrorPath());
        try {
            MyOtherAgent myAgent = new MyOtherAgent();
            myAgent.getLogService().info(() -> "Testing myAgent");

            GateFactory.register("testFileAgent");

            File expectedFile = new File(TestUtils.getErrorPath(), "MyOtherFluxName");
            Assertions.assertThat(expectedFile).doesNotExist();
            myAgent.register("testFileAgent", 5, 200, TestUtils.getErrorPath());
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
            TestUtils.sleep(300);

            Assertions.assertThat(testFlux).doesNotExist();
            Assertions.assertThat(new File(myAgent.getAcceptedPath(), "testFile")).exists();

            Assertions.assertThat(engine.getChannelSlots()).hasSize(1);
            Assertions.assertThat(engine.getPluggedChannels()).hasSize(1);
            myAgent.unregister();
            Assertions.assertThat(engine.getPluggedChannels()).isEmpty();
            Assertions.assertThat(engine.getChannelSlots()).hasSize(1);

        } finally {
            engine.shutdown();
        }
    }

    @Test
    public void testFileAgentReject() throws BaseEngineCreationException, IOException, InterruptedException {
        Engine engine = PM.get().createEngine("testFileAgent", TestUtils.getErrorPath());
        try {
            MyAgent myAgent = new MyAgent();

            myAgent.register("testFileAgent", 5, 200, TestUtils.getErrorPath());

            File testFlux = new File(myAgent.getWorkDir(), "rejectedFile");
            boolean created = testFlux.createNewFile();
            Assertions.assertThat(created).isTrue();
            Assertions.assertThat(testFlux).exists();

            engine.activateChannels();
            TestUtils.sleep(300);

            Assertions.assertThat(testFlux).doesNotExist();
            Assertions.assertThat(new File(myAgent.getRejectedPath(), "rejectedFile")).exists();

            myAgent.work("Invalid msg", null); // does nothing
        } finally {
            engine.shutdown();
        }
    }

    @Test(expected = InjectorNotCreatedYetException.class)
    public void testAgentNotRegistered() throws BaseEngineCreationException {
        Engine engine = PM.get().createEngine("testFileAgent", TestUtils.getErrorPath());
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

        MyFluxName flux = new MyFluxName();
        File file = new File(TestUtils.getErrorPath(), "testFile");
        flux.setFile(file);

        Assertions.assertThat(file).doesNotExist();
        agent.acceptFile(file);
        Assertions.assertThat(file).doesNotExist();

        Assertions.assertThat(file).doesNotExist();
        agent.rejectFile(file);
        Assertions.assertThat(file).doesNotExist();
    }

    // Utilities and classes

    private class MyFluxName extends AbstractImportFlux {

    }

    private class MyOtherFluxName extends AbstractImportFlux {

    }

    private class MyAgent extends AbstractImportAgent<MyFluxName> {

        MyAgent() {
            super(MyFluxName.class, MyFluxName.class.getSimpleName());
        }

        @Override
        public void doWork(MyFluxName flux, String engineUuid) {
            rejectFile(flux.getFile());
        }

        public void init() {
        }

        @Override
        protected MyFluxName createFlux() {
            return new MyFluxName();
        }
    }

    private class MyOtherAgent extends AbstractImportAgent<MyOtherFluxName> {

        MyOtherAgent() {
            super(MyOtherFluxName.class);
        }

        @Override
        public void doWork(MyOtherFluxName flux, String engineUuid) {
            acceptFile(flux.getFile());
        }

        @Override
        protected MyOtherFluxName createFlux() {
            return new MyOtherFluxName();
        }
    }
}
