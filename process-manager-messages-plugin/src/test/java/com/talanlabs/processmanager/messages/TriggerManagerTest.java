package com.talanlabs.processmanager.messages;

import com.talanlabs.processmanager.messages.trigger.ThreadedTrigger;
import com.talanlabs.processmanager.messages.trigger.TriggerEngine;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;
import com.talanlabs.processmanager.messages.trigger.api.TriggerManagerEventListener;
import com.talanlabs.processmanager.messages.trigger.event.BaseTriggerEvent;
import com.talanlabs.processmanager.messages.trigger.event.TriggerInstallEvent;
import com.talanlabs.processmanager.messages.trigger.tasks.AbstractTriggerTask;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TriggerManagerTest {

    @Test
    public void testTriggerManager() {
        TriggerEngine triggerEngine = TriggerEngine.getInstance();
        try {
            Checks checks = new Checks();
            TriggerManagerEventListener triggerManagerEventListener = event -> {
                if (event instanceof TriggerInstallEvent) {
                    checks.installEvent = true;
                    checks.isInstall = ((TriggerInstallEvent) event).isInstall();
                }
                checks.triggerManagerEventCalled = true;
            };
            TriggerEventListener triggerEventListener = event -> checks.triggerEventCalled = true;
            triggerEngine.setTriggerManagerEventListener(triggerManagerEventListener);
            triggerEngine.addListener(triggerEventListener);

            ThreadedTrigger trigger = new ThreadedTrigger("id", new AbstractTriggerTask() {
                @Override
                public void execute(TriggerEventListener triggerEventListener) {
                }

                @Override
                public void clean() {
                }

                @Override
                public String toString() {
                    return "id";
                }
            }, 2000);

            Assertions.assertThat(checks.triggerEventCalled).isFalse();
            BaseTriggerEvent<String> triggerEvent = new BaseTriggerEvent<>(trigger, "attachement");
            Assertions.assertThat(triggerEvent.getTimeStamp()).isNotNull();

            triggerEventListener.notifyEvent(triggerEvent);
            Assertions.assertThat(checks.triggerEventCalled).isTrue();

            triggerEngine.installTrigger(trigger, false);
            Assertions.assertThat(checks.triggerManagerEventCalled).isTrue();
            Assertions.assertThat(checks.installEvent).isTrue();
            Assertions.assertThat(checks.isInstall).isTrue();

            triggerEngine.uninstallTrigger("id");
            Assertions.assertThat(checks.isInstall).isFalse();

            triggerEngine.installTrigger(trigger, false);

            Assertions.assertThat(trigger.isActive()).isFalse();
            triggerEngine.activateTrigger("id");
            Assertions.assertThat(trigger.isActive()).isTrue();
            Assertions.assertThat(triggerEngine.getActiveTriggers()).containsExactly(trigger);
            triggerEngine.deactivateTrigger("id");
            Assertions.assertThat(trigger.isActive()).isFalse();
            Assertions.assertThat(triggerEngine.getActiveTriggers()).isEmpty();
        } finally {
            triggerEngine.shutdown();
        }
    }

    private class Checks {
        boolean triggerManagerEventCalled;
        boolean triggerEventCalled;
        boolean installEvent;
        boolean isInstall;
    }
}
