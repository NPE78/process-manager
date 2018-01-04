package com.talanlabs.processmanager.messages.trigger.event;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import java.io.File;

public class NewFileTriggerEvent extends FileTriggerEvent {

    public NewFileTriggerEvent(File f, Trigger source) {
        super(f, source);
    }

    @Override
    public String toString() {
        return "NEW FILE : " + getAttachment();
    }
}
