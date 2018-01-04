package com.talanlabs.processmanager.messages.gate;

import java.io.File;

public class GateFolders {

    private final File entranceFolder;
    private final File acceptedFolder;
    private final File rejectedFolder;
    private final File retryFolder;
    private final File archiveFolder;

    GateFolders(File entranceFolder, File acceptedFolder, File rejectedFolder, File retryFolder, File archiveFolder) {
        this.entranceFolder = entranceFolder;
        this.acceptedFolder = acceptedFolder;
        this.rejectedFolder = rejectedFolder;
        this.retryFolder = retryFolder;
        this.archiveFolder = archiveFolder;
    }

    public File getEntranceFolder() {
        return entranceFolder;
    }

    public File getAcceptedFolder() {
        return acceptedFolder;
    }

    public File getRejectedFolder() {
        return rejectedFolder;
    }

    public File getRetryFolder() {
        return retryFolder;
    }

    public File getArchiveFolder() {
        return archiveFolder;
    }
}
