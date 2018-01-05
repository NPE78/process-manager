package com.talanlabs.processmanager.messages.trigger.tasks;

import com.talanlabs.processmanager.messages.trigger.ThreadedTrigger;
import com.talanlabs.processmanager.messages.trigger.ThreadedTriggerTask;

public abstract class AbstractTriggerTask implements ThreadedTriggerTask {

    private ThreadedTrigger trigger;

    protected AbstractTriggerTask() {
    }

    protected ThreadedTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(ThreadedTrigger trigger) {
        this.trigger = trigger;
    }
}
