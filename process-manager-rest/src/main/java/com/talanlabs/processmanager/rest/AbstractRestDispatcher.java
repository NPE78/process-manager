package com.talanlabs.processmanager.rest;

import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.rest.agent.IRestAgent;
import com.talanlabs.processmanager.rest.model.LockedMessage;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import io.javalin.ApiBuilder;
import io.javalin.Context;
import io.javalin.HaltException;
import org.eclipse.jetty.http.HttpStatus;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * An abstract dispatcher which builds the route of some REST agents<br>
 * This agent is managing the maxWaiting number of REST contexts waiting for each REST agent<br>
 * The HTTP statuses can be:<br>
 * 405 (METHOD_NOT_ALLOWED) if the method is not allowed
 * 429 (TOO_MANY_REQUESTS) if the agent is overloaded
 * 503 (SERVICE_UNAVAILABLE) if the agent is not registered (not started) to an engine or the route is disconnected (stopped)
 */
public abstract class AbstractRestDispatcher implements IRestDispatcher {

    private final LogService logService;

    private final String name;

    private boolean activated;

    public AbstractRestDispatcher(String name) {
        logService = LogManager.getLogService(getClass());

        this.name = name;

        activated = true;
    }

    @Override
    public final String getName() {
        return name;
    }

    /**
     * This method is overridden to bind each action to an agent
     */
    @Override
    public final void addEndpoints() {
        dispatchGet(agentGet());
        dispatchPost(agentPost());
        dispatchPut(agentPut());
        dispatchPatch(agentPatch());
        dispatchDelete(agentDelete());
    }

    private void dispatchGet(IRestAgent agent) {
        ApiBuilder.get(context -> handle(context, agent));
    }

    private void dispatchPost(IRestAgent agent) {
        ApiBuilder.post(context -> handle(context, agent));
    }

    private void dispatchPut(IRestAgent agent) {
        ApiBuilder.put(context -> handle(context, agent));
    }

    private void dispatchPatch(IRestAgent agent) {
        ApiBuilder.patch(context -> handle(context, agent));
    }

    private void dispatchDelete(IRestAgent agent) {
        ApiBuilder.delete(context -> handle(context, agent));
    }

    private void handle(Context context, IRestAgent agent) {
        if (!activated) {
            throw new HaltException(HttpStatus.SERVICE_UNAVAILABLE_503, "Service is not activated");
        } else if (agent == null) {
            throw new HaltException(HttpStatus.METHOD_NOT_ALLOWED_405, "Method is not allowed");
        } else if (agent.getEngineUuid() == null) {
            throw new HaltException(HttpStatus.SERVICE_UNAVAILABLE_503, "Engine ");
        } else {
            doHandle(context, agent);
        }
    }

    private void doHandle(Context context, IRestAgent agent) {
        CountDownLatch cdl = null;
        Serializable message = agent.extract(context);

        synchronized (agent.getName().intern()) {
            Engine engine = PM.getEngine(agent.getEngineUuid());
            if (engine.getNbPending(agent.getName()) >= agent.getMaxWaiting()) {
                throw new HaltException(HttpStatus.TOO_MANY_REQUESTS_429, "Too many calls");
            } else {
                if (shouldLock()) {
                    cdl = synchronizedHandle(context, agent, engine, message);
                } else {
                    simpleHandle(agent, engine, message);
                }
            }
        }
        if (cdl != null) {
            waitForLock(cdl);
        }
    }

    private CountDownLatch synchronizedHandle(Context context, IRestAgent agent, Engine engine, Serializable message) {
        UUID lockId = UUID.randomUUID();
        CountDownLatch cdl = agent.lock(lockId, context); // do prior to handle message
        simpleHandle(agent, engine, new LockedMessage(lockId, message));
        return cdl;
    }

    private void simpleHandle(IRestAgent agent, Engine engine, Serializable message) {
        engine.handle(agent.getName(), message);
    }

    private void waitForLock(CountDownLatch cdl) {
        try {
            cdl.await(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logService.warn(() -> "Timeout exceeded for " + getName());
            throw new HaltException(HttpStatus.REQUEST_TIMEOUT_408, "Timeout");
        }
    }

    /**
     * Should the handle wait for an answer?
     */
    protected boolean shouldLock() {
        return true;
    }

    /**
     * The timeout in case the lock is used
     */
    protected long getTimeout() {
        return 120 * 1000; // 2min
    }


    protected IRestAgent agentGet() {
        return null;
    }

    protected IRestAgent agentPost() {
        return null;
    }

    protected IRestAgent agentPut() {
        return null;
    }

    protected IRestAgent agentPatch() {
        return null;
    }

    protected IRestAgent agentDelete() {
        return null;
    }

    @Override
    public void clear() {
        activated = false;
    }
}
