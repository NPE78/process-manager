package com.talanlabs.processmanager.messages.flux;

public abstract class AbstractFlux extends Flux {

    private int retry;

    public AbstractFlux() {
        this.retry = 0;
    }

    /**
     * Called by the RetryAgent
     */
    public boolean retry() {
        return ++retry < 6;
    }

    /**
     * Get the number of retries by the RetryAgent
     */
    public int getRetryNumber() {
        return retry;
    }

}
