package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ImportFluxTest {

    private final File basePath;

    public ImportFluxTest() throws IOException {
        File tempFile = File.createTempFile("baseEngineTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        basePath = new File(tmpFolder, UUID.randomUUID().toString());
        basePath.mkdir();

        tempFile.deleteOnExit();
        basePath.deleteOnExit();
    }

    @Test
    public void testImportFlux() throws IOException {

        String content = "test content";
        String filename = "testFlux.txt";

        File file = new File(basePath, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(Charset.forName("UTF-8")));
        }

        MyFlux flux = new MyFlux();
        flux.setFile(file);

        Assertions.assertThat(flux.getContent()).isEqualTo(content);
        Assertions.assertThat(flux.getFilename()).isEqualTo(filename);
    }

    private class MyFlux extends AbstractImportFlux {

    }
}
