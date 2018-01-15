package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.messages.flux.AbstractExportFlux;
import com.talanlabs.processmanager.shared.TestUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ExportFluxTest {

    @Test
    public void testExportFlux() throws IOException {

        String content = "test content";
        String filename = "testFlux.txt";

        File file = new File(TestUtils.getErrorPath(), filename);

        MyFlux flux = new MyFlux();
        Assertions.assertThat(flux.getFile()).isNull();
        flux.write(file, content);
        Assertions.assertThat(flux.getFile()).isNotNull();
        flux.getSendInformation().setValid(true);

        try (FileInputStream fis = new FileInputStream(file)){
            Assertions.assertThat(IOUtils.toString(fis, "UTF-8")).isEqualTo(content);
        }
        Assertions.assertThat(flux.getSendInformation().isValid()).isTrue();
        Assertions.assertThat(flux.getSendInformation().getMessageContent()).isEqualTo(content);
    }

    @Test(expected = IOException.class)
    public void testFailed() throws IOException {
        MyFlux flux = new MyFlux();
        flux.write(new File(""), "test");
    }

//    @Ignore("does't work on docker when user is root")
//    @Test(expected = IOException.class)
//    public void testReadOnly() throws IOException {
//        MyFlux flux = new MyFlux();
//        File file = new File(TestUtils.getErrorPath(), "readFile");
//        Assertions.assertThat(file.createNewFile()).isTrue();
//        Assertions.assertThat(file.setReadOnly()).isTrue();
//        Assertions.assertThat(file.setWritable(false, false)).isTrue();
//        Assertions.assertThat(file).exists();
//        flux.write(file, "test");
//    }

    private class MyFlux extends AbstractExportFlux {

    }
}
