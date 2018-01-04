package com.talanlabs.processmanager.messages.trigger.event;

import com.talanlabs.processmanager.messages.trigger.api.TriggerManager;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManagerEvent;

public class BaseTriggerManagerEvent implements TriggerManagerEvent {

    private final long tt;
    private final TriggerManager source;

    BaseTriggerManagerEvent(TriggerManager source) {
        tt = System.currentTimeMillis();
        this.source = source;
    }

    @Override
    public long getTimeStamp() {
        return tt;
    }

    @Override
    public TriggerManager getSource() {
        return source;
    }
}
