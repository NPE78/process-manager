package com.talanlabs.processmanager.rest.model;

import java.io.Serializable;
import java.util.UUID;

public final class LockedMessage implements Serializable {

    private final UUID lockId;

    private final Serializable message;

    public LockedMessage(UUID lockId, Serializable message) {
        this.lockId = lockId;
        this.message = message;
    }

    public UUID getLockId() {
        return lockId;
    }

    public Serializable getMessage() {
        return message;
    }
}
