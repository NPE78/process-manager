package com.talanlabs.processmanager.messages.injector;

public class InjectorDefinition {

    private String id;
    private String desc;
    private Class<?> agentClass;

    public InjectorDefinition(String id, String desc, Class<?> agentClass) {
        this.id = id;
        this.desc = desc;
        this.agentClass = agentClass;
    }

    public String getID() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public Class<?> getAgentClass() {
        return agentClass;
    }

}
