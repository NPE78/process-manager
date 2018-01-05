/*
 * Fichier : AbstractFileSysGate.java
 * Projet  : GPSTrainsServeurs
 * Date    : 2 dec. 2004
 * Auteur  : sps
 * -----------------------------------------------------------------------------
 * CVS :
 * $Header: /home/cvs_gpstrains/GPSTrainsServeurs/src/com/gpstrains/serveurs/AbstractFileSysGate.java,v 1.6 2005/02/16 11:05:15 bpt Exp $
 */
package com.talanlabs.processmanager.messages.gate;

import com.talanlabs.processmanager.messages.injector.MessageInjector;
import com.talanlabs.processmanager.messages.trigger.ThreadedTrigger;
import com.talanlabs.processmanager.messages.trigger.TriggerEngine;
import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;
import com.talanlabs.processmanager.messages.trigger.event.FileTriggerEvent;
import com.talanlabs.processmanager.messages.trigger.tasks.filesys.FolderEventTriggerTask;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractFileSysGate implements Gate {

    private static final String MOVE_IMPOSSIBLE_MSG = "IMPOSSIBLE TO MOVE THE FILE {0} TO {1}";
    private static final String FILE_DOESNOT_EXIST_MSG = "THE FILE {0} DOES NOT EXIST IN {1}";

    private final LogService logService;

    private final long retryPeriod;
    private final String name;
    private final File entranceFolder;
    private final File acceptedFolder;
    private final File rejectedFolder;
    private final File retryFolder;
    private final File archiveFolder;
    private final MessageInjector messageInjector;

    private boolean opened;

    AbstractFileSysGate(String name, GateFolders gateFolders, long retryPeriod, MessageInjector injector) {

        logService = LogManager.getLogService(getClass());

        this.name = name;
        this.entranceFolder = gateFolders.getEntranceFolder();
        this.acceptedFolder = gateFolders.getAcceptedFolder();
        this.rejectedFolder = gateFolders.getRejectedFolder();
        this.archiveFolder = gateFolders.getArchiveFolder();
        this.retryFolder = gateFolders.getRetryFolder();
        this.retryPeriod = retryPeriod;
        this.messageInjector = injector;
        this.messageInjector.setGate(this);
    }

    @Override
    public String getName() {
        return name;
    }

    void init() {
        if (mkdir(entranceFolder)) {
            logService.warn(() -> "Entrance Folder not created for " + name);
        }
        if (mkdir(acceptedFolder)) {
            logService.warn(() -> "Accepted Folder not created for " + name);
        }
        if (mkdir(rejectedFolder)) {
            logService.warn(() -> "Rejected Folder not created for " + name);
        }
        if (mkdir(archiveFolder)) {
            logService.warn(() -> "Archive Folder not created for " + name);
        }
        if (mkdir(retryFolder)) {
            logService.warn(() -> "Retry Folder not created for " + name);
        }

        open();

        if (retryPeriod > 0) {
            // install reply thread
            new RetryThread(retryPeriod, retryFolder, this).start();
        }
    }

    private boolean mkdir(File folder) {
        return !folder.exists() && !folder.mkdirs();
    }

    private File resolveEntranceFile(String msgID) {
        return new File(entranceFolder, msgID);
    }

    private File resolveAcceptedFile(String msgID) {
        return new File(acceptedFolder, msgID);
    }

    protected File resolveRejectedFile(String msgID) {
        return new File(rejectedFolder, msgID);
    }

    private File resolveRetryFile(String msgID) {
        return new File(retryFolder, msgID);
    }

    @Override
    public void createNewFile(String msgID, String data) {
        File nf = new File(entranceFolder, msgID);
        logService.debug(() -> "Creating new File : " + nf.getAbsolutePath());
        File lck = new File(entranceFolder, msgID + ".lck");
        try (OutputStream os = new FileOutputStream(nf)) {
            lck.createNewFile();
            os.write(data.getBytes());
            os.flush();
        } catch (IOException e) {
            logService.error(() -> "IOException", e);
        } finally {
            lck.delete();
        }
    }

    @Override
    public void reinject(String msgID) {
        File f = resolveRetryFile(msgID);
        if (f.exists()) {
            f.renameTo(resolveEntranceFile(msgID));
        }
    }

    @Override
    public void accept(String msgID) {
        File f = resolveEntranceFile(msgID);
        if (f.exists()) {
            boolean remove = f.renameTo(new File(acceptedFolder, msgID));
            if (!remove) {
                logService.warn(() -> MOVE_IMPOSSIBLE_MSG, msgID, f.getAbsolutePath());
            }
        }
    }

    @Override
    public void reject(String msgID) {
        File f = resolveEntranceFile(msgID);
        if (f.exists()) {
            boolean remove = f.renameTo(new File(rejectedFolder, msgID));
            if (remove) {
                logService.debug(() -> "REJET DU MESSAGE {0} ({1})", msgID, f.getAbsolutePath());
            } else {
                logService.warn(() -> MOVE_IMPOSSIBLE_MSG, msgID, f.getAbsolutePath());
            }
        } else {
            logService.warn(() -> FILE_DOESNOT_EXIST_MSG, msgID, f.getAbsolutePath());
        }
    }

    @Override
    public void retry(String msgID) {
        File f = resolveEntranceFile(msgID);
        logService.info(() -> "REESSAI DU MESSAGE {0} ({1})", msgID, f.getAbsolutePath());
        if (f.exists()) {
            boolean remove = f.renameTo(new File(retryFolder, msgID));
            if (remove) {
                logService.debug(() -> "REESSAI DU MESSAGE {0} ({1})", msgID, f.getAbsolutePath());
            } else {
                logService.warn(() -> MOVE_IMPOSSIBLE_MSG, msgID, f.getAbsolutePath());
            }
        } else {
            logService.warn(() -> "LE MESSAGE {0} N'EXISTE PAS DANS {1}", msgID, f.getAbsolutePath());
        }
    }

    @Override
    public void trash(String msgID) {
        File f = resolveAcceptedFile(msgID);
        if (f.exists()) {
            f.delete();
        }
    }

    @Override
    public void archive(String msgID) {
        File f = resolveAcceptedFile(msgID);
        if (f.exists()) {
            f.renameTo(new File(archiveFolder, msgID));
        }
    }

    @Override
    public void close() {
        TriggerEngine.getInstance().uninstallTrigger(name);
        opened = false;
    }

    @Override
    public boolean isOpened() {
        return opened;
    }

    @Override
    public void open() {
        // install and start trigger
        TriggerEngine te = TriggerEngine.getInstance();

        TriggerEventListener tel = evt -> messageInjector.inject((FileTriggerEvent) evt);

        logService.debug(() -> "ADD LISTENER TO TRIGGER ENGINE ON : " + entranceFolder.getAbsolutePath());
        te.addListener(tel);

        Trigger t = new ThreadedTrigger(name, new FolderEventTriggerTask(entranceFolder, ".lck"), 200);
        te.installTrigger(t, true);

        opened = true;
    }

    static class RetryThread extends Thread {

        private final LogService logService;

        private long retryPeriod;
        private File retryFolder;
        private Gate parentGate;

        RetryThread(long retryPeriod, File retryFolder, Gate parentGate) {
            super();
            setDaemon(true);

            logService = LogManager.getLogService(getClass());

            this.retryPeriod = retryPeriod;
            this.retryFolder = retryFolder;
            this.parentGate = parentGate;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    sleep(retryPeriod);
                    scanRetryFiles();
                }
            } catch (Exception ex) {
                logService.warn(() -> "ERROR {0} : {1}", ex, parentGate.getName(), ex.getMessage());
            }
        }

        /**
         * Scan files in the retry folder and reinject them
         */
        private void scanRetryFiles() {
            File[] f = retryFolder.listFiles();
            for (File aF : f) {
                if (aF.isFile()) {
                    parentGate.reinject(aF.getName());
                }
            }
        }
    }
}
