package com.talanlabs.processmanager.rest.agent;

import com.talanlabs.processmanager.shared.Agent;
import io.javalin.Context;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public interface IRestAgent extends Agent {

    /**
     * Returns the name of the agent, which is the one of the channel
     */
    String getName();

    /**
     * Return the maximum number of agents which can wait before any REST call is throwing error 503
     */
    int getMaxWaiting();

    /**
     * Extract data from the REST context
     */
    Serializable extract(Context context);

    String getEngineUuid();

    CountDownLatch lock(UUID lockId, Context context);

    void removeLock(UUID lockId);

    /**
     * Should the handle wait for an answer?
     */
    boolean shouldLock();

}
