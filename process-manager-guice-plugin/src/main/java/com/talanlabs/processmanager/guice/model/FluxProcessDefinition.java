package com.talanlabs.processmanager.guice.model;

import com.talanlabs.processmanager.messages.agent.IAgent;
import com.talanlabs.processmanager.messages.flux.IFlux;

public class FluxProcessDefinition<M extends IFlux> extends ProcessDefinition {

    private Class<M> fluxClass;

    private Class<? extends IAgent<M>> agentClass;

    public Class<M> getFluxClass() {
        return fluxClass;
    }

    public void setFluxClass(Class<M> fluxClass) {
        this.fluxClass = fluxClass;
    }

    public Class<? extends IAgent<M>> getAgentClass() {
        return agentClass;
    }

    public void setAgentClass(Class<? extends IAgent<M>> agentClass) {
        this.agentClass = agentClass;
    }
}
