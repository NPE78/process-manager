package com.talanlabs.processmanager.messages.flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractImportFlux extends AbstractFlux implements IImportFlux {

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private File file;

    private String content;

    public final File getFile() {
        return file;
    }

    public void setFile(File file) {
        if (file != null) {
            setFilename(file.getName());
        }
        this.file = file;
    }

    public String getContent() throws IOException {
        if (content == null) {
            content = readFile(file);
        }
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String readFile(File file) throws IOException {
        String result = null;
        if (file != null) {
            try (FileInputStream stream = new FileInputStream(file)) {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                /* Instead of using default, pass in a decoder. */
                result = UTF8_CHARSET.decode(bb).toString();
            }
        }
        return StringUtils.trimToEmpty(result);
    }
}
