package com.talanlabs.processmanager.messages.trigger;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEvent;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManager;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManagerEventListener;
import com.talanlabs.processmanager.messages.trigger.event.TriggerInstallEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple trigger manager which only contains triggers,
 */
public class SimpleTriggerManager implements TriggerManager {

    private final Map<String, Trigger> triggers;
    private TriggerEventListener triggerEventListener;
    private TriggerManagerEventListener triggerManagerEventListener;

    SimpleTriggerManager(TriggerManagerEventListener triggerManagerEventListener, TriggerEventListener triggerEventListener) {
        this.triggerManagerEventListener = triggerManagerEventListener;
        this.triggerEventListener = triggerEventListener;
        this.triggers = new HashMap<>();
    }

    public void installTrigger(Trigger trigger, boolean autoActivate) {
        synchronized (triggers) {
            triggers.put(trigger.getID(), trigger);
        }
        if (autoActivate) {
            trigger.activate(this);
        }
        triggerManagerEventListener.notifyEvent(new TriggerInstallEvent(this, trigger, true));
    }

    public void uninstallTrigger(String id) {
        Trigger trigger = getTrigger(id);
        if (trigger != null) {
            trigger.deactivate();
            synchronized (triggers) {
                triggers.remove(id);
            }
            triggerManagerEventListener.notifyEvent(new TriggerInstallEvent(this, trigger, false));
        }
    }

    public void activateTrigger(String id) {
        Trigger trigger = getTrigger(id);
        if (trigger != null) {
            trigger.activate(this);
        }
    }

    public void deactivateTrigger(String id) {
        Trigger trigger = getTrigger(id);
        if (trigger != null) {
            trigger.deactivate();
        }
    }

    public Trigger getTrigger(String id) {
        return triggers.get(id);
    }

    public List<Trigger> getActiveTriggers() {
        List<Trigger> lst = new ArrayList<>();
        synchronized (triggers) {
            for (Trigger t : triggers.values()) {
                if (t.isActive()) {
                    lst.add(t);
                }
            }
        }
        return lst;
    }

    public List<Trigger> getInactiveTriggers() {
        List<Trigger> lst = new ArrayList<>();
        synchronized (triggers) {
            for (Trigger t : triggers.values()) {
                if (!t.isActive()) {
                    lst.add(t);
                }
            }
        }
        return lst;
    }

    public void setTriggerEventListener(TriggerEventListener triggerEventListener) {
        this.triggerEventListener = triggerEventListener;
    }

    public void notifyEvent(TriggerEvent event) {
        triggerEventListener.notifyEvent(event);
    }

    public void setTriggerManagerEventListener(TriggerManagerEventListener triggerManagerEventListener) {
        this.triggerManagerEventListener = triggerManagerEventListener;
    }

    public void shutdown() {
        synchronized (triggers) {
            for (Trigger t : triggers.values()) {
                t.deactivate();
            }
        }
    }
}
