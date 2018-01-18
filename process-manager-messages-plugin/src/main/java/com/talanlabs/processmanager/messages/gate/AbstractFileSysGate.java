package com.talanlabs.processmanager.messages.gate;

import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.messages.exceptions.InvalidGateStateException;
import com.talanlabs.processmanager.messages.injector.IInjector;
import com.talanlabs.processmanager.messages.model.FluxFolders;
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
import java.nio.charset.Charset;
import java.util.stream.Stream;

public abstract class AbstractFileSysGate implements Gate {

    private static final String MOVE_IMPOSSIBLE_MSG = "IMPOSSIBLE TO MOVE THE FILE {0} TO {1}";
    private static final String FILE_DOESNOT_EXIST_MSG = "THE FILE {0} DOES NOT EXIST IN {1}";
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    private final LogService logService;

    private final String engineUuid;
    private final String name;
    private final FluxFolders fluxFolders;
    private final long retryPeriod;
    private final IInjector injector;

    private boolean opened;
    private RetryThread retryThread;

    AbstractFileSysGate(String engineUuid, String name, FluxFolders fluxFolders, long retryPeriod, IInjector injector) {

        logService = LogManager.getLogService(getClass());

        this.engineUuid = engineUuid;
        this.name = name;

        this.fluxFolders = fluxFolders;

        this.retryPeriod = retryPeriod;
        this.injector = injector;
        this.injector.setGate(this);

        fluxFolders.init();

        open();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public File getEntranceFolder() {
        return fluxFolders.getEntranceFolder();
    }

    private File resolveEntranceFile(String msgID) {
        return new File(fluxFolders.getEntranceFolder(), msgID);
    }

    private File resolveAcceptedFile(String msgID) {
        return new File(fluxFolders.getAcceptedFolder(), msgID);
    }

    protected File resolveRejectedFile(String msgID) {
        return new File(fluxFolders.getRejectedFolder(), msgID);
    }

    private File resolveRetryFile(String msgID) {
        return new File(fluxFolders.getRetryFolder(), msgID);
    }

    @Override
    public void createNewFile(String msgID, String data) {
        File nf = new File(fluxFolders.getEntranceFolder(), msgID);
        logService.debug(() -> "Creating new File : " + nf.getAbsolutePath());
        File lck = new File(fluxFolders.getEntranceFolder(), msgID + ".lck");
        try (OutputStream os = new FileOutputStream(nf)) {
            if (!lck.createNewFile()) {
                logService.warn(() -> "Create lock {0} failed.", msgID);
            }
            os.write(data.getBytes(CHARSET_UTF8));
            os.flush();
        } catch (IOException e) {
            logService.error(() -> "IOException", e);
        } finally {
            if (!lck.delete()) {
                logService.warn(() -> "Delete lock {0} failed.", msgID);
            }
        }
    }

    @Override
    public void reinject(String msgID) {
        File f = resolveRetryFile(msgID);
        if (f.exists()) {
            boolean moved = f.renameTo(resolveEntranceFile(msgID));
            if (!moved) {
                logService.warn(() -> MOVE_IMPOSSIBLE_MSG, msgID, f.getAbsolutePath());
            }
        } else {
            logService.warn(() -> "Reinject {0} failed.", msgID);
        }
    }

    @Override
    public void trash(String msgID) {
        File f = resolveAcceptedFile(msgID);
        if (f.exists()) {
            boolean removed = f.delete();
            if (!removed) {
                logService.warn(() -> "Trash {0} failed.", msgID);
            }
        } else {
            logService.warn(() -> FILE_DOESNOT_EXIST_MSG, msgID, f.getAbsolutePath());
        }
    }

    @Override
    public void archive(String msgID) {
        File f = resolveAcceptedFile(msgID);
        moveFileToFolder(f, msgID, fluxFolders.getArchiveFolder());
    }

    @Override
    public void accept(String msgID) {
        File f = resolveEntranceFile(msgID);
        moveFileToFolder(f, msgID, fluxFolders.getAcceptedFolder());
    }

    private void moveFileToFolder(File sourceFolder, String filename, File folder) {
        if (sourceFolder.exists()) {
            boolean remove = sourceFolder.renameTo(new File(folder, filename));
            if (!remove) {
                logService.warn(() -> MOVE_IMPOSSIBLE_MSG, filename, sourceFolder.getAbsolutePath());
            }
        } else {
            logService.warn(() -> FILE_DOESNOT_EXIST_MSG, filename, sourceFolder.getAbsolutePath());
        }
    }

    @Override
    public void reject(String msgID) {
        File f = resolveEntranceFile(msgID);
        if (f.exists()) {
            boolean remove = f.renameTo(new File(fluxFolders.getRejectedFolder(), msgID));
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
        logService.info(() -> "RETRYING FILE {0} ({1})", msgID, f.getAbsolutePath());
        if (f.exists()) {
            boolean remove = f.renameTo(new File(fluxFolders.getRetryFolder(), msgID));
            if (remove) {
                logService.debug(() -> "RETRYING FILE {0} ({1})", msgID, f.getAbsolutePath());
            } else {
                logService.warn(() -> MOVE_IMPOSSIBLE_MSG, msgID, f.getAbsolutePath());
            }
        } else {
            logService.warn(() -> "FILE {0} DOES NOT EXIST IN {1}", msgID, f.getAbsolutePath());
        }
    }

    @Override
    public void close() {
        PM.getEngine(engineUuid).getAddon(TriggerEngine.class)
                .ifPresent(te -> te.uninstallTrigger(name));
        opened = false;
    }

    @Override
    public boolean isOpened() {
        return opened;
    }

    @Override
    public void open() {
        if (isOpened()) {
            throw new InvalidGateStateException("The gate is already opened");
        }
        if (retryThread != null) {
            throw new InvalidGateStateException("Retry thread is still active");
        }
        // install and start trigger
        TriggerEngine triggerEngine = PM.getEngine(engineUuid).getAddon(TriggerEngine.class)
                .orElseGet(() -> TriggerEngine.register(engineUuid));

        TriggerEventListener tel = evt -> injector.inject((FileTriggerEvent) evt);

        logService.debug(() -> "ADD LISTENER TO TRIGGER ENGINE ON: " + fluxFolders.getEntranceFolder().getAbsolutePath());
        triggerEngine.addListener(tel);

        Trigger t = new ThreadedTrigger(name, new FolderEventTriggerTask(fluxFolders.getEntranceFolder(), ".lck"), 200);
        triggerEngine.installTrigger(t, true);

        opened = true;

        if (retryPeriod > 0) {
            // install retry thread
            retryThread = new RetryThread(retryPeriod, fluxFolders.getRetryFolder(), this);
            retryThread.start();
        }
    }

    private class RetryThread extends Thread {

        private final LogService logService;

        private final long retryPeriod;
        private final File retryFolder;
        private final Gate parentGate;

        private RetryThread(long retryPeriod, File retryFolder, Gate parentGate) {
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
                while (opened) {
                    sleep(retryPeriod);
                    scanRetryFiles();
                }
            } catch (Exception ex) {
                logService.warn(() -> "ERROR {0} : {1}", ex, parentGate.getName(), ex.getMessage());
            } finally {
                retryThread = null;
            }
        }

        /**
         * Scan files in the retry folder and reinject them
         */
        private void scanRetryFiles() {
            File[] f = retryFolder.listFiles();
            if (f == null) {
                return;
            }
            Stream.of(f).filter(File::isFile).forEach(file -> parentGate.reinject(file.getName()));
        }
    }
}
