package com.talanlabs.processmanager.messages.trigger.api;

import java.util.List;

/**
 * Trigger manager interface
 */
public interface TriggerManager extends TriggerEventListener {

    /**
     * Install a trigger
     * @param trigger the trigger to install
     * @param autoActivate if true, activates the trigger
     */
    void installTrigger(Trigger trigger, boolean autoActivate);

    void uninstallTrigger(String id);

    void activateTrigger(String id);

    void deactivateTrigger(String id);

    Trigger getTrigger(String id);

    List<Trigger> getActiveTriggers();

    List<Trigger> getInactiveTriggers();

    void setTriggerManagerEventListener(TriggerManagerEventListener triggerManagerEventListener);

    void setTriggerEventListener(TriggerEventListener triggerEventListener);

    void shutdown();
}
