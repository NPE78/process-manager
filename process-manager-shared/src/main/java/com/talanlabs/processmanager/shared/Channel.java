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
     * @return The number of agents threads working right now
     */
    int getNbWorking();

    /**
     * @return The number of agents threads waiting to be activated
     */
    int getNbPending();

    boolean isLocal();

    /**
     * Activates the channel
     */
    boolean activate();

    /**
     * At least one agent is working
     */
    boolean isBusy();

}
