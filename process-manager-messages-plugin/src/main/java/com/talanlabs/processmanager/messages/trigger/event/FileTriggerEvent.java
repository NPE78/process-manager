package com.talanlabs.processmanager.messages.trigger.event;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import java.io.File;

public class FileTriggerEvent extends BaseTriggerEvent<File> {

    public FileTriggerEvent(File file, Trigger source) {
        super(source, file);
    }

    public boolean isNewFileEvent() {
        return false;
    }
}
