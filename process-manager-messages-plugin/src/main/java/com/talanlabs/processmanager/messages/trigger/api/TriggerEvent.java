package com.talanlabs.processmanager.messages.trigger.api;

public interface TriggerEvent {

    /**
     * Timestamp at which the event has been created
     */
    long getTimeStamp();

    /**
     * Returns the trigger for which the event has been created
     */
    Trigger getSource();

    Object getAttachment();

}
