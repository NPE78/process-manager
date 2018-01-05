package com.talanlabs.processmanager.messages.trigger.event;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import java.io.File;

public final class ModifiedFileTriggerEvent extends FileTriggerEvent {

    public ModifiedFileTriggerEvent(File f, Trigger source) {
        super(f, source);
    }

    @Override
    public String toString() {
        return "MODIFIED FILE : " + getAttachment();
    }
}
