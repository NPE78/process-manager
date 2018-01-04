package com.talanlabs.processmanager.messages.trigger;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;

public abstract class AbstractTrigger implements Trigger {

    private boolean isActive;
    private String id;

    AbstractTrigger(String id) {
        isActive = false;
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public final synchronized void activate(TriggerEventListener triggerEventListener) {
        if (!isActive) {
            isActive = true;
            doActivate(triggerEventListener);
        }
    }

    public final synchronized void deactivate() {
        if (isActive) {
            isActive = false;
            doDeactivate();
        }
    }

    /**
     * Override this method to do stuff when deactivate is called
     */
    protected void doDeactivate() {
    }

    /**
     * Override this method to do stuff when activate is called
     * @param triggerEventListener the trigger event listener
     */
    protected abstract void doActivate(TriggerEventListener triggerEventListener);

    public boolean isActive() {
        return isActive;
    }
}
