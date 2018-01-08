package com.talanlabs.processmanager.messages.gate;

import com.talanlabs.processmanager.messages.agent.AbstractFileAgent;
import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.messages.injector.AbstractInjector;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class InjectorTest {

    private final File basePath;

    public InjectorTest() throws IOException {
        File tempFile = File.createTempFile("baseEngineTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        basePath = new File(tmpFolder, UUID.randomUUID().toString());
        basePath.mkdir();

        tempFile.deleteOnExit();
        basePath.deleteOnExit();
    }

    @Test
    public void testInjector() throws IOException, InterruptedException {
        GateFactory gateFactory = new GateFactory();
        try {
            MyInjector<MyFlux> myInjector = new MyInjector<>(MyFlux.class);
            gateFactory.buildGate("injectorTest", 500, myInjector);

            File file = new File(myInjector.getWorkDir(), "testFile");
            Assertions.assertThat(file.createNewFile()).isTrue();
            File expectedFile = new File(myInjector.getAcceptedPath(), "testFile");
            Assertions.assertThat(expectedFile).doesNotExist();

            sleep(1200);

            Assertions.assertThat(file).doesNotExist();
            Assertions.assertThat(expectedFile).exists();

            File expectedNewFile = new File(myInjector.getWorkDir(), "accepted/newFile");
            myInjector.getGate().createNewFile("newFile", "my awesome content");
            sleep(600);
            Assertions.assertThat(expectedNewFile).exists();

            myInjector.getGate().trash("newFile");
            Assertions.assertThat(expectedNewFile).doesNotExist();
        } finally {
            gateFactory.closeGates();
        }
    }

    @Test
    public void testFolders() throws IOException {
        GateFactory gateFactory = new GateFactory();
        MyInjector<MyFlux> myInjector = new MyInjector<>(MyFlux.class);
        gateFactory.buildGate("injectorTest", 500, myInjector);
        gateFactory.closeGates();

        File accepted = new File(myInjector.getWorkDir(), "testAccepted");
        File retry = new File(myInjector.getWorkDir(), "testRetry");
        File rejected = new File(myInjector.getWorkDir(), "testRejected");
        File archive = new File(myInjector.getWorkDir(), "accepted/testArchive");

        Assertions.assertThat(accepted.createNewFile()).isTrue();
        Assertions.assertThat(retry.createNewFile()).isTrue();
        Assertions.assertThat(rejected.createNewFile()).isTrue();
        Assertions.assertThat(archive.createNewFile()).isTrue();

        File expectedAccepted = new File(myInjector.getAcceptedPath(), accepted.getName());
        Assertions.assertThat(expectedAccepted).doesNotExist();
        myInjector.getGate().accept(accepted.getName());
        Assertions.assertThat(expectedAccepted).exists();

        File expectedRetry = new File(myInjector.getRetryPath(), retry.getName());
        Assertions.assertThat(expectedRetry).doesNotExist();
        myInjector.getGate().retry(retry.getName());
        Assertions.assertThat(expectedRetry).exists();

        File expectedRejected = new File(myInjector.getRejectedPath(), rejected.getName());
        Assertions.assertThat(expectedRejected).doesNotExist();
        myInjector.getGate().reject(rejected.getName());
        Assertions.assertThat(expectedRejected).exists();

        File expectedArchive = new File(myInjector.getArchivePath(), archive.getName());
        Assertions.assertThat(expectedArchive).doesNotExist();
        myInjector.getGate().archive(archive.getName());
        Assertions.assertThat(expectedArchive).exists();
    }

    @Test
    public void testRetry() throws IOException, InterruptedException {
        GateFactory gateFactory = new GateFactory();
        try {
            MyInjector<MyFlux> myInjector = new MyInjector<>(MyFlux.class);
            gateFactory.buildGate("injectorTest", 500, myInjector);

            File expectedFile = new File(myInjector.getAcceptedPath(), "testFile");
            Assertions.assertThat(expectedFile).doesNotExist();

            File file = new File(myInjector.getWorkDir(), "retry/testFile");
            Assertions.assertThat(file.createNewFile()).isTrue();

            sleep(1200);
            Assertions.assertThat(file).doesNotExist();
            Assertions.assertThat(expectedFile).exists();
        } finally {
            gateFactory.closeGates();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidFlux() {
        new MyAgent<>(null);
    }

    // Utilities and classes

    private void sleep(int ms) throws InterruptedException {
        new CountDownLatch(1).await(ms, TimeUnit.MILLISECONDS);
    }

    private static class MyFlux extends AbstractImportFlux {
        public MyFlux() {
        }
    }

    private static class MyInvalidFlux extends AbstractImportFlux {
        public MyInvalidFlux() {
        }
    }

    private class MyAgent<E extends AbstractImportFlux> extends AbstractFileAgent<E> {

        private final LogService logService;

        private final Class<E> fluxClass;

        private MyAgent(Class<E> fluxClass) {
            super(fluxClass);

            logService = LogManager.getLogService(getClass());

            this.fluxClass = fluxClass;
        }

        @Override
        protected E createFlux() {
            try {
                return fluxClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void doWork(E flux, String engineUuid) {
            File file = flux.getFile();
            File dest = new File(getAcceptedPath(), file.getName());
            boolean renamed = file.renameTo(dest);
            logService.info(() -> "file renamed to {0}? {1}", dest.getAbsolutePath(), renamed);
            Assertions.assertThat(renamed).isTrue();
        }
    }

    private class MyInjector<E extends AbstractImportFlux> extends AbstractInjector<E> {

        private final LogService logService;
        private final Class<E> fluxClass;

        private MyInjector(Class<E> fluxClass) {
            super(fluxClass.getSimpleName(), basePath.getAbsolutePath());

            logService = LogManager.getLogService(getClass());

            this.fluxClass = fluxClass;
        }

        @Override
        protected void handleFlux(E flux) {
            File file = flux.getFile();
            File dest = new File(getAcceptedPath(), file.getName());
            boolean renamed = file.renameTo(dest);
            logService.info(() -> "file renamed to {0}? {1}", dest.getAbsolutePath(), renamed);
            Assertions.assertThat(renamed).isTrue();
        }

        @Override
        public E createFlux() {
            try {
                return fluxClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
