package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.messages.flux.AbstractExportFlux;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ExportFluxTest {

    private final File basePath;

    public ExportFluxTest() throws IOException {
        File tempFile = File.createTempFile("baseEngineTest", "tmp");
        File tmpFolder = tempFile.getParentFile();
        basePath = new File(tmpFolder, UUID.randomUUID().toString());
        basePath.mkdir();

        tempFile.deleteOnExit();
        basePath.deleteOnExit();
    }

    @Test
    public void testExportFlux() throws IOException {

        String content = "test content";
        String filename = "testFlux.txt";

        File file = new File(basePath, filename);

        MyFlux flux = new MyFlux();
        Assertions.assertThat(flux.getFile()).isNull();
        flux.write(file, content);
        Assertions.assertThat(flux.getFile()).isNotNull();
        flux.getSendInformation().setValid(true);

        try (FileInputStream fis = new FileInputStream(file)){
            Assertions.assertThat(IOUtils.toString(fis, "UTF-8")).isEqualTo(content);
        }
        Assertions.assertThat(flux.getFilename()).isEqualTo(filename);
        Assertions.assertThat(flux.getSendInformation().isValid()).isTrue();
        Assertions.assertThat(flux.getSendInformation().getMessageContent()).isEqualTo(content);
    }

    private class MyFlux extends AbstractExportFlux {

    }
}
