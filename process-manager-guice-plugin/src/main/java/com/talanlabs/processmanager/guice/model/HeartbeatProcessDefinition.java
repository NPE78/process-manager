package com.talanlabs.processmanager.guice.model;

import com.talanlabs.processmanager.shared.Agent;

public class HeartbeatProcessDefinition {

    private Integer seconds;
    private String beat;
    private Class<? extends Agent> agentClass;

    public Integer getSeconds() {
        return seconds;
    }

    public void setSeconds(Integer seconds) {
        this.seconds = seconds;
    }

    public String getBeat() {
        return beat;
    }

    public void setBeat(String beat) {
        this.beat = beat;
    }

    public Class<? extends Agent> getAgentClass() {
        return agentClass;
    }

    public void setAgentClass(Class<? extends Agent> agentClass) {
        this.agentClass = agentClass;
    }
}
