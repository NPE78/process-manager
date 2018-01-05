package com.talanlabs.processmanager.messages.trigger.event;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import java.io.File;

public final class RemovedFileTriggerEvent extends FileTriggerEvent {

    public RemovedFileTriggerEvent(File f, Trigger source) {
        super(f, source);
    }

    @Override
    public String toString() {
        return "REMOVED FILE : " + getAttachment();
    }
}
