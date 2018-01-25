package com.talanlabs.processmanager.rest;

import com.google.common.annotations.VisibleForTesting;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.rest.agent.IRestAgent;
import com.talanlabs.processmanager.rest.exceptions.RestAgentException;
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
 * This dispatcher is managing the maxWaiting number of REST contexts waiting for each REST agent<br>
 * The HTTP statuses can be:<br>
 * 405 (METHOD_NOT_ALLOWED) if the method is not allowed<br>
 * 410 (GONE), only when the agent is synchronized (see {@link IRestAgent#shouldLock()}), is thrown when the agent times out ({@link #getTimeout()})<br>
 * 429 (TOO_MANY_REQUESTS) if the agent is overloaded<br>
 * 503 (SERVICE_UNAVAILABLE) if the agent is not registered (not started) to an engine or the route is disconnected (stopped)
 */
public abstract class AbstractRestDispatcher implements IRestDispatcher {

    private final LogService logService;

    private final String name;

    private boolean activated;

    private final Object getLock = new Object();
    private final Object postLock = new Object();
    private final Object putLock = new Object();
    private final Object patchLock = new Object();
    private final Object deleteLock = new Object();
    private final Object defaultLock = new Object();

    private IRestAgent agentGet;
    private IRestAgent agentPost;
    private IRestAgent agentPut;
    private IRestAgent agentPatch;
    private IRestAgent agentDelete;

    private long timeout;

    public AbstractRestDispatcher(String name) {
        logService = LogManager.getLogService(getClass());

        this.name = name;

        this.timeout = 120L * 1000L; // default timeout is 2min
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
        activated = true;

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

    @VisibleForTesting
    protected final Object getLock(String method) {
        switch (method) {
            case "GET":
                return getLock;
            case "POST":
                return postLock;
            case "PUT":
                return putLock;
            case "PATCH":
                return patchLock;
            case "DELETE":
                return deleteLock;
            default:
                return defaultLock;
        }
    }

    private void doHandle(Context context, IRestAgent agent) {
        CountDownLatch cdl = null;
        Serializable message = agent.extract(context);

        synchronized (getLock(context.request().getMethod())) {
            Engine engine = PM.getEngine(agent.getEngineUuid());
            int nbPending = engine.getNbPending(agent.getName());
            int maxWaiting = agent.getMaxWaiting();
            if ((nbPending >= maxWaiting && maxWaiting != 0) || (maxWaiting == 0 && engine.isBusy(agent.getName()))) {
                throw new HaltException(HttpStatus.TOO_MANY_REQUESTS_429, "Too many calls");
            } else {
                if (agent.shouldLock()) {
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
            boolean await = cdl.await(getTimeout(), TimeUnit.MILLISECONDS);
            if (!await) {
                logService.warn(() -> "Timeout exceeded for " + getName());
                throw new HaltException(HttpStatus.REQUEST_TIMEOUT_408, "Timeout");
            }
        } catch (InterruptedException e) {
            logService.warn(() -> getName() + " has been interrupted");
            throw new HaltException(HttpStatus.GONE_410, "Interrupted");
        }
    }

    public long getTimeout() {
        return timeout;
    }

    /**
     * The timeout in case the lock is used
     */
    public final void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /* package protected */
    final IRestAgent agentGet() {
        return agentGet;
    }

    /* package protected */
    final IRestAgent agentPost() {
        return agentPost;
    }

    /* package protected */
    final IRestAgent agentPut() {
        return agentPut;
    }

    /* package protected */
    final IRestAgent agentPatch() {
        return agentPatch;
    }

    /* package protected */
    final IRestAgent agentDelete() {
        return agentDelete;
    }

    /* package protected */
    final void setAgentGet(IRestAgent agentGet) {
        protectAgentSetter();
        this.agentGet = agentGet;
    }

    /* package protected */  void setAgentPost(IRestAgent agentPost) {
        this.agentPost = agentPost;
    }

    /* package protected */  void setAgentPut(IRestAgent agentPut) {
        this.agentPut = agentPut;
    }

    /* package protected */  void setAgentPatch(IRestAgent agentPatch) {
        this.agentPatch = agentPatch;
    }

    /* package protected */  void setAgentDelete(IRestAgent agentDelete) {
        this.agentDelete = agentDelete;
    }

    private void protectAgentSetter() {
        if (activated) {
            throw new RestAgentException("Dispatcher is already active. It is impossible to set new REST agent");
        }
    }

    @Override
    public void clear() {
        activated = false;
    }
}
