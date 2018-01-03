package com.talanlabs.processmanager.engine;

public class AbstractChannel {

    private String name;

    AbstractChannel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean activate() {
        return true;
    }
}
