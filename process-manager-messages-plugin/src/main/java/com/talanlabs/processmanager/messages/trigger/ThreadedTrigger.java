package com.talanlabs.processmanager.messages.trigger;

import com.talanlabs.processmanager.messages.trigger.api.TriggerEventListener;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;

public class ThreadedTrigger extends AbstractTrigger {

    private final ThreadedTriggerTask task;
    private final long wait;

    public ThreadedTrigger(String id, ThreadedTriggerTask task, long wait) {
        super(id);
        this.task = task;
        this.wait = wait;
        this.task.setTrigger(this);
    }

    public void doActivate(TriggerEventListener triggerEventListener) {
        task.clean();
        HostingThread hostingThread = new HostingThread(this, task, triggerEventListener, wait);
        hostingThread.start();
    }

    @Override
    public String toString() {
        return "THREADED TRIGGER (task = " + task.toString() + ")";
    }

    private class HostingThread extends Thread {

        private final LogService logService;

        private final ThreadedTrigger trigger;
        private final ThreadedTriggerTask task;
        private final TriggerEventListener triggerEventListener;
        private long wait;

        private HostingThread(ThreadedTrigger trigger, ThreadedTriggerTask task, TriggerEventListener triggerEventListener, long wait) {
            super("TriggerThread_" + trigger.getId());
            setDaemon(true);

            logService = LogManager.getLogService(getClass());

            this.trigger = trigger;
            this.task = task;
            this.triggerEventListener = triggerEventListener;
            this.wait = wait;
        }

        @Override
        public void run() {
            try {
                while (trigger.isActive()) {
                    task.execute(triggerEventListener);
                    Thread.sleep(wait);
                }
            } catch (InterruptedException e) {
                logService.error(() -> "Trigger {0} stopped! InterruptedException", e, ThreadedTrigger.this.getId());
            }
            task.clean();
        }
    }
}
