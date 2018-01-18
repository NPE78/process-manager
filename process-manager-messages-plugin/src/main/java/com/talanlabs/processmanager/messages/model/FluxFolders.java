package com.talanlabs.processmanager.messages.model;

import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;

public class FluxFolders {

    private final LogService logService;

    private final File entranceFolder;
    private final File acceptedFolder;
    private final File rejectedFolder;
    private final File retryFolder;
    private final File archiveFolder;

    FluxFolders(File entranceFolder, File acceptedFolder, File rejectedFolder, File retryFolder, File archiveFolder) {

        logService = LogManager.getLogService(getClass());

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

    public void init() {
        if (mkdir(entranceFolder)) {
            logService.warn(() -> "Entrance Folder not created");
        }
        if (mkdir(acceptedFolder)) {
            logService.warn(() -> "Accepted Folder not created");
        }
        if (mkdir(rejectedFolder)) {
            logService.warn(() -> "Rejected Folder not created");
        }
        if (mkdir(archiveFolder)) {
            logService.warn(() -> "Archive Folder not created");
        }
        if (mkdir(retryFolder)) {
            logService.warn(() -> "Retry Folder not created");
        }
    }

    private boolean mkdir(File folder) {
        return !folder.exists() && !folder.mkdirs();
    }

    public static FluxFolders from(File rootDir) {
        return new FluxFolders(rootDir,
                new File(rootDir + "/accepted"),
                new File(rootDir + "/rejected"),
                new File(rootDir + "/retry"),
                new File(rootDir + "/archive"));
    }
}
