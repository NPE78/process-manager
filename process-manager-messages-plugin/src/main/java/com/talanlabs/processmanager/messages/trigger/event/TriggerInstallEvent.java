package com.talanlabs.processmanager.messages.trigger.event;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManager;

/**
 * Triggered when a trigger is installed or uninstalled
 */
public final class TriggerInstallEvent extends BaseTriggerManagerEvent {

    private final Trigger trigger;
    private final boolean isInstall;

    /**
     * An install or uninstall event
     * @param source the trigger manager which manages the trigger
     * @param trigger the installed or uninstalled trigger
     * @param isInstall if true, it's a install event. Otherwise, it's a uninstall event
     */
    public TriggerInstallEvent(TriggerManager source, Trigger trigger, boolean isInstall) {
        super(source);
        this.trigger = trigger;
        this.isInstall = isInstall;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * @return true if it is a install event, false if uninstall event
     */
    public boolean isInstall() {
        return isInstall;
    }

    public String toString() {
        return "Trigger '" + trigger.toString() + "' " + (isInstall() ? "Installed" : "Uninstalled");
    }
}
