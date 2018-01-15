package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.shared.TestUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ImportFluxTest {

    @Test
    public void testImportFlux() throws IOException {

        String content = "test content";
        String filename = "testFlux.txt";

        File file = new File(TestUtils.getErrorPath(), filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(Charset.forName("UTF-8")));
        }

        MyFlux flux = new MyFlux();
        Assertions.assertThat(flux.getFile()).isNull();
        flux.setFile(file);
        Assertions.assertThat(flux.getFile()).isNotNull();

        Assertions.assertThat(flux.getContent()).isEqualTo(content);
        Assertions.assertThat(flux.getContent()).isEqualTo(content);
    }

    @Test
    public void testNoFileFlux() throws IOException {
        MyFlux flux = new MyFlux();
        flux.setFile(null);
        Assertions.assertThat(flux.getContent()).isEmpty();
    }

    private class MyFlux extends AbstractImportFlux {

    }
}
