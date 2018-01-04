package com.talanlabs.processmanager.messages.flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public abstract class AbstractImportFlux extends AbstractFlux implements IImportFlux {

    private File file;

    private String content;

    public AbstractImportFlux() {
        super();
    }

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
                result = Charset.forName("UTF-8").decode(bb).toString();
            }
        }
        return result;
    }
}
