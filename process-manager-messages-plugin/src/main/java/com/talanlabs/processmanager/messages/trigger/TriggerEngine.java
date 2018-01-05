package com.talanlabs.processmanager.messages.trigger;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEvent;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManager;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManagerEventListener;
import java.util.LinkedList;
import java.util.List;

public class TriggerEngine implements TriggerManager {

    private final TriggerManager manager;
    private final TriggerEventListenerBroker triggerEventListenerBroker;

    private TriggerEngine() {
        triggerEventListenerBroker = new TriggerEventListenerBroker();

        this.manager = new SimpleTriggerManager(event -> {
        }, triggerEventListenerBroker);
    }

    public static TriggerEngine getInstance() {
        return TriggerEngine.SingletonHolder.instance;
    }

    public void addListener(TriggerEventListener listener) {
        triggerEventListenerBroker.addListener(listener);
    }

    public void activateTrigger(String id) {
        manager.activateTrigger(id);
    }

    public void deactivateTrigger(String id) {
        manager.deactivateTrigger(id);
    }

    public List<Trigger> getActiveTriggers() {
        return manager.getActiveTriggers();
    }

    public List<Trigger> getInactiveTriggers() {
        return manager.getInactiveTriggers();
    }

    public Trigger getTrigger(String id) {
        return manager.getTrigger(id);
    }

    public void installTrigger(Trigger trigger, boolean autoActivate) {
        manager.installTrigger(trigger, autoActivate);
    }

    public void notifyEvent(TriggerEvent event) {
        manager.notifyEvent(event);
    }

    public void setTriggerManagerEventListener(TriggerManagerEventListener triggerManagerEventListener) {
        manager.setTriggerManagerEventListener(triggerManagerEventListener);
    }

    public void setTriggerEventListener(TriggerEventListener triggerEventListener) {
        manager.setTriggerEventListener(triggerEventListener);
    }

    public void uninstallTrigger(String id) {
        manager.uninstallTrigger(id);
    }

    public void shutdown() {
        manager.shutdown();
    }

    /**
     * Sécurité anti-désérialisation
     */
    private Object readResolve() {
        return getInstance();
    }

    /**
     * Holder
     */
    private static final class SingletonHolder {
        private static final TriggerEngine instance = new TriggerEngine();
    }

    private static class TriggerEventListenerBroker implements TriggerEventListener {

        private final List<TriggerEventListener> listeners;

        TriggerEventListenerBroker() {
            listeners = new LinkedList<>();
        }

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
