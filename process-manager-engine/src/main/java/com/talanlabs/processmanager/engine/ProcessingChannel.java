package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.engine.handlereport.BaseLocalHandleReport;
import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Channel;
import com.talanlabs.processmanager.shared.ChannelSlot;
import com.talanlabs.processmanager.shared.HandleReport;
import com.talanlabs.processmanager.shared.PluggableChannel;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ProcessingChannel extends AbstractChannel implements PluggableChannel {

    private static final int NO_LOG = -1;

    private static final int IDLING_LOG = 0;
    private static final int BUSY_LOG = 1;
    private final LogService logService;

    private int maxWorkingAgents;
    private Agent agent;

    private boolean overloaded = false;
    private boolean busy = false;
    private int lastLoggedBusy = NO_LOG;

    private final Status status;
    private ProcessPool processPool;

    private boolean available = false;

    public ProcessingChannel(String name, int maxWorking, Agent agent) {
        super(name);

        this.maxWorkingAgents = maxWorking;
        this.agent = agent;

        logService = LogManager.getLogServiceFactory().getLogService(getClass());
        status = new Status(this);
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isBusy() {
        return busy;
    }

    @Override
    public boolean isOverloaded() {
        return overloaded;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        this.available = available && processPool != null;
    }

    @Override
    public boolean activate() {
        ProcessMonitor pm = new ProcessMonitor(this);
        processPool = new ProcessPool(status, pm);
        pm.start();
        available = true;
        return true;
    }

    @Override
    public void shutdown() {
        processPool.shutdown();
    }

    @Override
    public int getNbWorking() {
        return status.runningCount();
    }

    @Override
    public String toString() {
        return "Processing Channel " + maxWorkingAgents + ", " + agent.getClass().getName() + ", " + status.runningCount() + " running, " + status.pendingCount() + " pending";
    }

    private void notifyIdling() {
        busy = false;

        switch (lastLoggedBusy) {
            case NO_LOG:
            case BUSY_LOG:
                logService.debug(() -> "Idling for service " + getName());
                break;

            default:
                break;
        }
        lastLoggedBusy = IDLING_LOG;
    }

    private void notifyBusy() {
        busy = true;

        switch (lastLoggedBusy) {
            case NO_LOG:
            case IDLING_LOG:
                logService.debug(() -> "Busy for service " + getName());
                break;

            default:
                break;
        }
        lastLoggedBusy = BUSY_LOG;
    }

    @Override
    public HandleReport acceptMessage(Serializable message, ChannelSlot slot) {

        AgentRunnable agt = new AgentRunnable(agent, message, processPool);
        if (processPool == null) {
            throw new NullPointerException("process pool is null for " + agent);
        }
        processPool.newProcess(agt);
        return new BaseLocalHandleReport(slot, agt);
    }

    private class AgentRunnable implements Runnable {
        private Agent agent;
        private Serializable message;
        private ProcessPool processPool;

        private AgentRunnable(Agent agent, Serializable message, ProcessPool processPool) {
            this.agent = agent;
            this.message = message;
            this.processPool = processPool;
        }

        @Override
        public void run() {
            try {
                agent.work(message);
            } catch (Exception ex) {
                logService.error(() -> "Uncatched exception in agent work : " + agent.getClass().getName(), ex);
            } finally {
                processPool.doneProcess(Thread.currentThread());
            }
        }
    }

    private static class AgentThread extends Thread {

        private ProcessingChannel pchannel;

        private AgentThread(AgentRunnable runnable, ProcessingChannel pchannel) {
            super(runnable, "AGTWORK_" + pchannel.getName());

            this.pchannel = pchannel;

            // this thread is not a daemon thread! It must finished for the process manager to completely shut down
        }

        @Override
        public void run() {
            pchannel.notifyBusy();

            super.run();
        }
    }

    private class Status {

        private final List<AgentRunnable> pendingList;
        private final List<Thread> runningList;
        private final ProcessingChannel processingChannel;

        private Status(ProcessingChannel processingChannel) {
            this.pendingList = new LinkedList<>();
            this.runningList = new LinkedList<>();
            this.processingChannel = processingChannel;
        }

        boolean pushToWork(int i) {
            AgentRunnable runnable;
            synchronized (pendingList) {
                if (pendingList.size() <= i) {
                    return false;
                }
                runnable = pendingList.remove(i);
            }
            Thread thread = new AgentThread(runnable, processingChannel);
            synchronized (runningList) {
                runningList.add(thread);
            }
            thread.start();

            return true;
        }

        void pushToWait(AgentRunnable runnable) {
            synchronized (pendingList) {
                pendingList.add(runnable);
            }
        }

        void workDone(Thread thread) {
            synchronized (runningList) {
                runningList.remove(thread);
                if (runningList.isEmpty()) {
                    processingChannel.notifyIdling();
                }
            }
        }

        int pendingCount() {
            return pendingList.size();
        }

        int runningCount() {
            return runningList.size();
        }

    }

    private class ProcessMonitor extends Thread {

        private final Semaphore semaphore;

        private boolean running;

        private ProcessMonitor(Channel channel) {
            super("PMONITOR_" + channel.getName());
            setDaemon(true);

            semaphore = new Semaphore(1);
            try {
                // we want the semaphore to be consumed at first
                semaphore.acquire();
            } catch (InterruptedException e) {
                logService.warn(() -> "Error initializing semaphore", e);
            }

            running = false;
        }

        @Override
        public void run() {
            running = true;
            try {
                // while the channel is running or there are still pending messages, we loop
                while (isActive()) {
                    sleep();
                    pushToWork();
                }
            } catch (Exception e) {
                logService.error(() -> "InterruptedException " + ProcessingChannel.this.getName(), e);
            }

            // work is finished, we clear information
            processPool = null;
            available = false;
        }

        private void pushToWork() throws InterruptedException {
            while (isActive() && status.runningCount() < maxWorkingAgents) {
                try {
                    if (!status.pushToWork(0)) {
                        break;
                    }
                } catch (Exception e) {
                    logService.error(() -> "PUSH TO WORK " + ProcessingChannel.this.getName(), e);
                    sleep();
                }
            }
        }

        private boolean isActive() {
            return running || status.pendingCount() > 0;
        }

        /**
         * Sleep for 5 secs
         *
         * @throws InterruptedException an exception is thrown if the wait is interrupted, one way or another
         */
        private void sleep() throws InterruptedException {
            synchronized (this) {
                semaphore.tryAcquire(5, TimeUnit.SECONDS);
            }
        }

        protected void wakeUp() {
            semaphore.release();
        }

        public void shutdown() {
            running = false;

            int pendingCount = status.pendingCount();
            if (pendingCount > 0) {
                logService.warn(() -> "ProcessingChannel {0} still has {1} message(s) waiting to be processed", ProcessingChannel.this.getName(), pendingCount);
            } else {
                logService.info(() -> "ProcessingChannel {0} has been shut down", ProcessingChannel.this.getName());
            }

            wakeUp();
        }
    }

    private class ProcessPool {

        private final Status status;
        private final ProcessMonitor monitor;

        private ProcessPool(Status status, ProcessMonitor monitor) {
            this.status = status;
            this.monitor = monitor;
        }

        void newProcess(AgentRunnable runnable) {
            status.pushToWait(runnable);
            notifyMonitor();
        }

        void notifyMonitor() {
            synchronized (monitor) {
                monitor.wakeUp();
            }
        }

        void doneProcess(Thread thread) {
            status.workDone(thread);
            notifyMonitor();
        }

        void shutdown() {
            monitor.shutdown();
        }
    }
}
