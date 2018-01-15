package com.talanlabs.processmanager.messages.flux;

public abstract class Flux implements IFlux {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
