package com.talanlabs.processmanager.messages.agent;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.messages.flux.AbstractFlux;
import com.talanlabs.processmanager.shared.Agent;
import java.io.Serializable;

public final class RetryAgent implements Agent {

    @Override
    public void work(Serializable message, String engineUuid) {
        if (message instanceof AbstractFlux) {
            AbstractFlux flux = (AbstractFlux) message;
            if (flux.retry()) {
                ProcessManager.handle(engineUuid, flux.getName(), message);
            }
        }
    }
}
