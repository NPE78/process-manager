package com.talanlabs.processmanager.messages.agent;

import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.messages.flux.AbstractFlux;
import com.talanlabs.processmanager.shared.Agent;
import java.io.Serializable;

public final class RetryAgent implements Agent {

    private final String engineUuid;

    public RetryAgent(String engineUuid) {
        this.engineUuid = engineUuid;
    }

    @Override
    public void work(Serializable message) {
        if (message instanceof AbstractFlux) {
            AbstractFlux flux = (AbstractFlux) message;
            if (flux.retry()) {
                PM.handle(engineUuid, flux.getName(), message);
            }
        }
    }
}
