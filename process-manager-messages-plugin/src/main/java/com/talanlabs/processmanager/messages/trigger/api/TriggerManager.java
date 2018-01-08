package com.talanlabs.processmanager.messages.trigger.api;

import java.util.List;

/**
 * Trigger manager interface
 */
public interface TriggerManager extends TriggerEventListener {

    /**
     * Install a trigger and notify an {@link com.talanlabs.processmanager.messages.trigger.event.TriggerInstallEvent} on the triggerManagerEventListener (see setTriggerManagerEventListener)
     *
     * @param trigger      the trigger to install, which has a unique id
     * @param autoActivate if true, activates the trigger
     */
    void installTrigger(Trigger trigger, boolean autoActivate);

    /**
     * Uninstall a trigger and notify a {@link com.talanlabs.processmanager.messages.trigger.event.TriggerInstallEvent} on the triggerManagerEventListener (see setTriggerManagerEventListener)
     *
     * @param id the id of the trigger to uninstall
     */
    void uninstallTrigger(String id);

    /**
     *
     * @param id
     */
    void activateTrigger(String id);

    void deactivateTrigger(String id);

    Trigger getTrigger(String id);

    List<Trigger> getActiveTriggers();

    List<Trigger> getInactiveTriggers();

    void setTriggerManagerEventListener(TriggerManagerEventListener triggerManagerEventListener);

    void setTriggerEventListener(TriggerEventListener triggerEventListener);

    void shutdown();

}
