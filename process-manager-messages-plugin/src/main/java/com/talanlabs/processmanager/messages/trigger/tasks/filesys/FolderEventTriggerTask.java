package com.talanlabs.processmanager.messages.trigger.tasks.filesys;

import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;
import com.talanlabs.processmanager.messages.trigger.event.ModifiedFileTriggerEvent;
import com.talanlabs.processmanager.messages.trigger.event.NewFileTriggerEvent;
import com.talanlabs.processmanager.messages.trigger.event.RemovedFileTriggerEvent;
import com.talanlabs.processmanager.messages.trigger.tasks.AbstractTriggerTask;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderEventTriggerTask extends AbstractTriggerTask {

    private static final String STOP_FILE = "trigger.stop"; //$NON-NLS-1$

    private final Map<String, Long> lmdates;
    private final File folder;
    private final String lockExtension;
    private final File stopFile;

    public FolderEventTriggerTask(File folder, String lockExtension) {
        this.folder = folder;
        this.lmdates = new HashMap<>();
        this.lockExtension = lockExtension;
        this.stopFile = new File(folder, STOP_FILE);
    }

    @Override
    public void execute(TriggerEventListener triggerEventListener) {
        if (stopFile.exists()) {
            return;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (stopFile.exists()) {
                    return;
                }
                if (acceptScan(file)) {
                    propagateEvent(file, triggerEventListener);
                }
            }
        }
        List<String> toRemove = new ArrayList<>();
        for (String fname : lmdates.keySet()) {
            File file = new File(folder, fname);
            if (!file.exists()) {
                toRemove.add(fname);
            }
        }
        for (String fname : toRemove) {
            lmdates.remove(fname);
            triggerEventListener.notifyEvent(new RemovedFileTriggerEvent(new File(folder, fname), getTrigger()));
        }
    }

    private boolean acceptScan(File f) {
        if (f.isDirectory()) {
            return false;
        }
        if (f.getName().equals(STOP_FILE)) {
            return false;
        }
        // it's a standard file, is there a lock? if there is, ignore
        return !f.getName().endsWith(lockExtension) && !new File(folder, f.getName() + lockExtension).exists();
    }

    private void propagateEvent(File f, TriggerEventListener triggerEventListener) {
        long lmdiff = getLastModifDateDiff(f);
        if (lmdiff < 0) {
            // This file is new
            // Propagate a "new file" message
            triggerEventListener.notifyEvent(new NewFileTriggerEvent(f, getTrigger()));
        } else if (lmdiff > 0) {
            // This file has been modified
            // Propagate a "modified file" message
            triggerEventListener.notifyEvent(new ModifiedFileTriggerEvent(f, getTrigger()));
        }
    }

    private long getLastModifDateDiff(File f) {
        long lmd = f.lastModified();
        Object obj = lmdates.get(f.getName());
        if (obj == null) {
            lmdates.put(f.getName(), lmd);
            return -1;
        } else {
            long delta = lmd - (Long) obj;

            if (delta > 0) {
                lmdates.put(f.getName(), lmd);
            }
            return delta;
        }
    }

    @Override
    public void clean() {
        lmdates.clear();
    }

}
