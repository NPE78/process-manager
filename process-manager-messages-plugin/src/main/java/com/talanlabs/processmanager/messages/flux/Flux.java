package com.talanlabs.processmanager.messages.flux;

import com.talanlabs.processmanager.messages.listener.IMessageListener;
import java.io.Serializable;

public abstract class Flux implements Serializable {

    private String name;

    private String filename;

    /**
     * Return the name of the flux (based on the {@link com.talanlabs.processmanager.messages.model.annotation.Flux} annotation)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Add a listener to detect the end of the treatment of a message
     */
    abstract void addMessageListener(IMessageListener messageListener);

    /**
     * Add a listener to detect the end of the treatment of a message, given a priority
     */
    abstract void addMessageListener(int index, IMessageListener messageListener);

    abstract void fireMessageListener(IMessageListener.Status status);

}
