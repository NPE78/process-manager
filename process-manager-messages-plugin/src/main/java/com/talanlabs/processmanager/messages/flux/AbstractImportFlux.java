package com.talanlabs.processmanager.messages.flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractImportFlux extends AbstractFlux implements IImportFlux {

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private File file;

    private String content;

    public final File getFile() {
        return file;
    }

    public void setFile(File file) {
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
            result = getFileContent(file);
        }
        return StringUtils.trimToEmpty(result);
    }

    private String getFileContent(File file) throws IOException {
        String result;
        try (FileInputStream fis = new FileInputStream(file)) {
            result = IOUtils.readLines(fis, UTF8_CHARSET).stream().collect(Collectors.joining("\n"));
        }
        return result;
    }
}
