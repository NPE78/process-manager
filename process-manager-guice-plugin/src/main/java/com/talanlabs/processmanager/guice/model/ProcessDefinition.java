package com.talanlabs.processmanager.guice.model;

public class ProcessDefinition {

    private String name;

    private int maxWorking;

    private int maxWaiting;

    private ProcessType processType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxWorking() {
        return maxWorking;
    }

    public void setMaxWorking(int maxWorking) {
        this.maxWorking = maxWorking;
    }

    public int getMaxWaiting() {
        return maxWaiting;
    }

    public void setMaxWaiting(int maxWaiting) {
        this.maxWaiting = maxWaiting;
    }

    public ProcessType getProcessType() {
        return processType;
    }

    public void setProcessType(ProcessType processType) {
        this.processType = processType;
    }
}
