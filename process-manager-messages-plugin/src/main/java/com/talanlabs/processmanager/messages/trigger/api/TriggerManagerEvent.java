package com.talanlabs.processmanager.messages.trigger.api;

public interface TriggerManagerEvent {

    /**
     * Timestamp at which the event has been created
     */
    long getTimeStamp();

    /**
     * Returns the trigger manager for which the event has been created
     */
    TriggerManager getSource();

}
