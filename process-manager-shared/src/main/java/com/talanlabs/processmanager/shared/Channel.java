package com.talanlabs.processmanager.shared;

public interface Channel {

    /**
     * @return The name of the channel, which must be unique
     */
    String getName();

    /**
     * Is the channel available, ie able to process a message ?
     *
     * @return true if this channel is available.
     */
    boolean isAvailable();

    void setAvailable(boolean available);

    /**
     * @return The number of agents working right now
     */
    int getNbWorking();

    boolean isLocal();

    boolean activate();

    /**
     * At least one agent is working
     */
    boolean isBusy();

}
