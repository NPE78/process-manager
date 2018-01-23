package com.talanlabs.processmanager.rest.agent;

import com.talanlabs.processmanager.engine.AbstractAgent;
import com.talanlabs.processmanager.rest.exceptions.RestAgentException;
import com.talanlabs.processmanager.rest.model.LockedInfo;
import com.talanlabs.processmanager.rest.model.LockedMessage;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import io.javalin.Context;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractRestAgent extends AbstractAgent implements IRestAgent {

    private final LogService logService;

    private final Map<UUID, LockedInfo> synchronizationMap;

    public AbstractRestAgent(String name) {
        super(name);

        logService = LogManager.getLogService(getClass());

        this.synchronizationMap = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public CountDownLatch lock(UUID lockId, Context context) {
        synchronized (synchronizationMap) {
            if (synchronizationMap.containsKey(lockId)) {
                throw new RestAgentException("This lock id is already used");
            }
            LockedInfo lockedInfo = LockedInfo.of(context);
            synchronizationMap.put(lockId, lockedInfo);
            return lockedInfo.getCountDownLatch();
        }
    }

    @Override
    public final void work(Serializable message) {
        if (message instanceof LockedMessage) {
            manageLockedMessage((LockedMessage) message);
        } else {
            doWork(message, null);
        }
    }

    private void manageLockedMessage(LockedMessage lockedMessage) {
        UUID lockId = lockedMessage.getLockId();
        LockedInfo lockedInfo = synchronizationMap.get(lockId);
        try {
            if (lockedInfo != null && lockedInfo.getLockedCount() > 0) {
                doWork(lockedMessage.getMessage(), lockedInfo.getContext());
            } else {
                // if count down is consumed, the dispatcher has removed it because of a timeout, we do nothing but log
                logService.warn(() -> "The message has expired");
            }
        } finally {
            removeLock(lockId);
        }
    }

    @Override
    public final void removeLock(UUID lockId) {
        synchronized (synchronizationMap) {
            LockedInfo removed = synchronizationMap.remove(lockId);
            if (removed != null && removed.getLockedCount() > 0L) {
                removed.countDown();
            }
        }
    }

    @Override
    public boolean shouldLock() {
        return true;
    }

    protected abstract void doWork(Serializable message, Context context);

}
