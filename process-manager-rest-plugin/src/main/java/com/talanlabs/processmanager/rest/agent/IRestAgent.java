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
     * Extract data from the REST context. This Serializable object is then passed to the {@link #work(Serializable)} method.
     */
    Serializable extract(Context context);

    /**
     * The engine uuid on which the agent is registered
     */
    String getEngineUuid();

    /**
     * Called by the dispatcher in case of a synchronous agent
     * @param lockId this id will be used by the dispatcher to release the lock if an exception occurred or the timeout expires. See {@link #removeLock(UUID)}
     * @param context the context which must be used to update accordingly
     * @return a count down latch which is used by the dispatcher to wait for the process to finish
     */
    CountDownLatch lock(UUID lockId, Context context);

    /**
     * Consume the lock corresponding to the given lockId (built by {@link #lock(UUID, Context)})
     */
    void removeLock(UUID lockId);

    /**
     * Should the handle wait for an answer? This determines if the agent is synchronous (true) or asynchronous (false)
     */
    boolean shouldLock();

}
