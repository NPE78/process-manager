package com.talanlabs.processmanager.messages.trigger.api;

/**
 * Interface of any trigger
 */
public interface Trigger {

    /**
     * Unique ID of the trigger
     */
    String getId();

    /**
     * Is the trigger active?
     * @return true if active, false otherwise
     */
    boolean isActive();

    /**
     * Activates the trigger
     * @param triggerEventListener the trigger event listener to link it with the trigger
     */
    void activate(TriggerEventListener triggerEventListener);

    void deactivate();
}
