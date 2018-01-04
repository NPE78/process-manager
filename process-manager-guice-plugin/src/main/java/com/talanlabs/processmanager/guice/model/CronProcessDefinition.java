package com.talanlabs.processmanager.guice.model;

import com.talanlabs.processmanager.shared.Agent;

public class CronProcessDefinition {

    private String schedulingPattern;
    private Class<? extends Agent> agentClass;

    public String getSchedulingPattern() {
        return schedulingPattern;
    }

    public void setSchedulingPattern(String schedulingPattern) {
        this.schedulingPattern = schedulingPattern;
    }

    public Class<? extends Agent> getAgentClass() {
        return agentClass;
    }

    public void setAgentClass(Class<? extends Agent> agentClass) {
        this.agentClass = agentClass;
    }
}
