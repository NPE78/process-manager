package com.talanlabs.processmanager.messages.trigger;

import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;

public interface ThreadedTriggerTask {

    void execute(TriggerEventListener triggerEventListener);

    void setTrigger(ThreadedTrigger trigger);

    void clean();

}
