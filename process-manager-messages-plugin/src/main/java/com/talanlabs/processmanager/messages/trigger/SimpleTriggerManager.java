package com.talanlabs.processmanager.messages.trigger;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEvent;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManager;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManagerEventListener;
import com.talanlabs.processmanager.messages.trigger.event.TriggerInstallEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simple trigger manager which only contains triggers,
 */
public class SimpleTriggerManager implements TriggerManager {

    private final Map<String, Trigger> triggers;
    private TriggerEventListener triggerEventListener;
    private TriggerManagerEventListener triggerManagerEventListener;

    SimpleTriggerManager(TriggerManagerEventListener triggerManagerEventListener, TriggerEventListener triggerEventListener) {
        this.triggers = new HashMap<>();
        this.triggerEventListener = triggerEventListener;
        this.triggerManagerEventListener = triggerManagerEventListener;
    }

    @Override
    public void installTrigger(Trigger trigger, boolean autoActivate) {
        synchronized (triggers) {
            triggers.put(trigger.getId(), trigger);
        }
        if (autoActivate) {
            trigger.activate(this);
        }
        triggerManagerEventListener.notifyEvent(new TriggerInstallEvent(this, trigger, true));
    }

    @Override
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

    @Override
    public void activateTrigger(String id) {
        Trigger trigger = getTrigger(id);
        if (trigger != null) {
            trigger.activate(this);
        }
    }

    @Override
    public void deactivateTrigger(String id) {
        Trigger trigger = getTrigger(id);
        if (trigger != null) {
            trigger.deactivate();
        }
    }

    @Override
    public Trigger getTrigger(String id) {
        return triggers.get(id);
    }

    @Override
    public List<Trigger> getActiveTriggers() {
        synchronized (triggers) {
            return triggers.values().stream().filter(Trigger::isActive).collect(Collectors.toList());
        }
    }

    @Override
    public List<Trigger> getInactiveTriggers() {
        synchronized (triggers) {
            return triggers.values().stream().filter(t -> !t.isActive()).collect(Collectors.toList());
        }
    }

    @Override
    public void setTriggerEventListener(TriggerEventListener triggerEventListener) {
        this.triggerEventListener = triggerEventListener;
    }

    @Override
    public void notifyEvent(TriggerEvent event) {
        triggerEventListener.notifyEvent(event);
    }

    @Override
    public void setTriggerManagerEventListener(TriggerManagerEventListener triggerManagerEventListener) {
        this.triggerManagerEventListener = triggerManagerEventListener;
    }

    @Override
    public void shutdown() {
        synchronized (triggers) {
            triggers.values().forEach(Trigger::deactivate);
        }
    }
}
