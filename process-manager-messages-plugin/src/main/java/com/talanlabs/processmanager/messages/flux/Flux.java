package com.talanlabs.processmanager.messages.flux;

public abstract class Flux implements IFlux {

    private String name;

    private String filename;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    void setFilename(String filename) {
        this.filename = filename;
    }
}
