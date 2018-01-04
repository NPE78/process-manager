package com.talanlabs.processmanager.guice.model;

import com.talanlabs.processmanager.shared.Agent;

public class SimpleProcessDefinition extends ProcessDefinition {

    private Class<? extends Agent> agentClass;

    public Class<? extends Agent> getAgentClass() {
        return agentClass;
    }

    public void setAgentClass(Class<? extends Agent> agentClass) {
        this.agentClass = agentClass;
    }
}
