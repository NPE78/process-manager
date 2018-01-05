package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.messages.gate.GateFactory;
import com.talanlabs.processmanager.messages.injector.AbstractInjector;
import com.talanlabs.processmanager.messages.model.annotation.Flux;
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
            MyInjector myInjector = new MyInjector();
            gateFactory.buildGate("injectorTest", 500, myInjector);

            File file = new File(myInjector.getWorkDir(), "testFile");
            Assertions.assertThat(file.createNewFile()).isTrue();
            File successFile = new File(myInjector.getAcceptedPath(), "testFile");
            Assertions.assertThat(successFile).doesNotExist();

            sleep(1000);

            Assertions.assertThat(file).doesNotExist();
            Assertions.assertThat(successFile).exists();
        } finally {
            gateFactory.closeGates();
        }
    }

    // Utilities and classes

    private void sleep(int ms) throws InterruptedException {
        new CountDownLatch(1).await(ms, TimeUnit.MILLISECONDS);
    }

    @Flux(fluxCode = "injectorTest")
    private class MyFlux extends AbstractImportFlux {

    }

    private class MyInjector extends AbstractInjector<MyFlux> {

        private final LogService logService;

        private MyInjector() {
            super(MyFlux.class, basePath.getAbsolutePath());

            logService = LogManager.getLogService(getClass());
        }

        @Override
        protected void handleFlux(MyFlux flux) {
            File file = flux.getFile();
            File dest = new File(getAcceptedPath(), file.getName());
            boolean renamed = file.renameTo(dest);
            logService.info(() -> "file renamed to {0}? {1}", dest.getAbsolutePath(), renamed);
            Assertions.assertThat(renamed).isTrue();
        }

        @Override
        public MyFlux createFlux() {
            return new MyFlux();
        }
    }
}
