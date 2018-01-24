package com.talanlabs.processmanager.messages.trigger;

import com.google.common.annotations.VisibleForTesting;
import com.talanlabs.processmanager.engine.EngineAddon;
import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEvent;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManager;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManagerEventListener;

import java.util.LinkedList;
import java.util.List;

/**
 * The trigger engine is the default trigger manager with the process-manager-messages-plugin.
 */
public final class TriggerEngine extends EngineAddon<TriggerEngine> implements TriggerManager {

    private final TriggerManager manager;
    private final TriggerEventListenerBroker triggerEventListenerBroker;

    @VisibleForTesting
    /* package protected */ TriggerEngine(String engineUuid) {
        super(TriggerEngine.class, engineUuid);

        triggerEventListenerBroker = new TriggerEventListenerBroker();

        this.manager = new SimpleTriggerManager(event -> {
        }, triggerEventListenerBroker);
    }

    public static TriggerEngine register(String engineUuid) {
        return new TriggerEngine(engineUuid).registerAddon();
    }

    @Override
    public void disconnectAddon() {
        shutdown();
    }

    public void addListener(TriggerEventListener listener) {
        triggerEventListenerBroker.addListener(listener);
    }

    @Override
    public void activateTrigger(String id) {
        manager.activateTrigger(id);
    }

    @Override
    public void deactivateTrigger(String id) {
        manager.deactivateTrigger(id);
    }

    @Override
    public List<Trigger> getActiveTriggers() {
        return manager.getActiveTriggers();
    }

    @Override
    public List<Trigger> getInactiveTriggers() {
        return manager.getInactiveTriggers();
    }

    @Override
    public Trigger getTrigger(String id) {
        return manager.getTrigger(id);
    }

    @Override
    public void installTrigger(Trigger trigger, boolean autoActivate) {
        manager.installTrigger(trigger, autoActivate);
    }

    @Override
    public void notifyEvent(TriggerEvent event) {
        manager.notifyEvent(event);
    }

    @Override
    public void setTriggerManagerEventListener(TriggerManagerEventListener triggerManagerEventListener) {
        manager.setTriggerManagerEventListener(triggerManagerEventListener);
    }

    @Override
    public void setTriggerEventListener(TriggerEventListener triggerEventListener) {
        manager.setTriggerEventListener(triggerEventListener);
    }

    @Override
    public void uninstallTrigger(String id) {
        manager.uninstallTrigger(id);
    }

    @Override
    public void shutdown() {
        manager.shutdown();
    }

    private static class TriggerEventListenerBroker implements TriggerEventListener {

        private final List<TriggerEventListener> listeners;

        TriggerEventListenerBroker() {
            listeners = new LinkedList<>();
        }

        @Override
        public void notifyEvent(TriggerEvent event) {
            for (TriggerEventListener listener : listeners) {
                listener.notifyEvent(event);
            }
        }

        void addListener(TriggerEventListener tel) {
            listeners.add(tel);
        }

        public void removeListener(TriggerEventListener tel) {
            listeners.remove(tel);
        }
    }
}
