package com.talanlabs.processmanager.messages.flux;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

public abstract class AbstractExportFlux extends AbstractFlux implements IExportFlux {

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    private File file;

    private SendInformation sendInformation;

    public final File getFile() {
        return file;
    }

    public final void write(File file, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            IOUtils.write(content, fos, CHARSET_UTF8);
        }
        getSendInformation().setMessageContent(content);
        this.file = file;
    }

    public final SendInformation getSendInformation() {
        if (this.sendInformation == null) {
            this.sendInformation = new SendInformation();
        }
        return this.sendInformation;
    }
}
