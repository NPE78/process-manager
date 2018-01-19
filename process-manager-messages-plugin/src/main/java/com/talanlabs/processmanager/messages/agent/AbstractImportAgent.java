package com.talanlabs.processmanager.messages.agent;

import com.talanlabs.processmanager.engine.AbstractAgent;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.messages.exceptions.InjectorNotCreatedYetException;
import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.messages.gate.GateFactory;
import com.talanlabs.processmanager.messages.injector.AbstractInjector;
import com.talanlabs.processmanager.messages.injector.IInjector;
import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.Serializable;
import java.util.Optional;

/**
 * An agent wrapping a more simple agent: AbstractAgent
 *
 * @param <M> the type of flux managed by this agent
 */
public abstract class AbstractImportAgent<M extends AbstractImportFlux> implements Agent {

    private final LogService logService;

    private final SimpleAgent simpleAgent; // wrapped so that some methods are hidden
    private final Class<M> fluxClass;
    private IInjector injector;

    public AbstractImportAgent(Class<M> fluxClass) {
        this(fluxClass, fluxClass.getSimpleName());
    }

    protected AbstractImportAgent(Class<M> fluxClass, String name) {
        super();

        this.simpleAgent = new SimpleAgent(name);

        logService = LogManager.getLogService(getClass());

        this.fluxClass = fluxClass;
    }

    public final LogService getLogService() {
        return logService;
    }

    /**
     * The name can be overridden to permit the use of underlying agents
     *
     * @return the name of the scanned folder
     */
    public String getName() {
        return simpleAgent.getName();
    }

    /**
     * Builds an injector which simply handles any file event back to this agent, using the channel
     */
    protected IInjector buildInjector(String engineUuid, File basePath) {
        injector = new FileInjector(engineUuid, getName(), basePath); // done twice with #register, in case buildInjector is called separately
        // we could check a file injector wasn't set yet
        return injector;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void work(Serializable message) {
        if (isConcerned(message)) {
            doWork((M) message);
        } else {
            logService.warn(() -> "Agent {0} received a message it could not understand: {1}", getName(), message != null ? message.getClass() : "null");
        }
    }

    private boolean isConcerned(Serializable message) {
        return message != null && fluxClass.isAssignableFrom(message.getClass());
    }

    public abstract void doWork(M flux);

    /**
     * Register this agent as a agent which is triggered to manage files.<br>
     * Creates an injector, creates a channel, and registers to the gate factory addon (creates one if none is bound)
     *
     * @param engineUuid the unique id of the engine
     * @param maxWorking the maximum number of working agents
     * @param delay      the delay between each check of the filesystem, in ms
     * @param basePath   the base path where the files for this agent are located, before being redirected to accepted, rejected, retry or archive folders
     */
    public void register(String engineUuid, int maxWorking, long delay, File basePath) {
        simpleAgent.register(engineUuid, maxWorking);

        Engine engine = PM.getEngine(engineUuid);
        GateFactory gateFactory = engine.getAddon(GateFactory.class)
                .orElseGet(() -> GateFactory.register(engineUuid));
        injector = buildInjector(engineUuid, basePath);
        gateFactory.buildGate(getName(), delay, injector);
    }

    /**
     * Unregister from the current engine and stop the channel
     */
    public final void unregister() {
        simpleAgent.unregister();
    }

    protected abstract M createFlux();

    /**
     * This method is called by the injector. It must not be called by an agent thread
     */
    protected void handleFlux(M flux, String engineUuid) {
        PM.getEngine(engineUuid).handle(getName(), flux);
    }

    protected Agent getAgent() {
        return this;
    }

    /**
     * Returns the working folder of the injector/agent. This is the folder which is monitored for new messages
     */
    public final File getWorkDir() {
        return Optional.ofNullable(injector).map(IInjector::getWorkDir).orElseThrow(InjectorNotCreatedYetException::new);
    }

    /**
     * Returns the folder where the messages are moved if they are accepted
     */
    public final String getAcceptedPath() {
        return Optional.ofNullable(injector).map(IInjector::getAcceptedPath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    /**
     * Returns the folder where the messages are moved if they are rejected
     */
    public final String getRejectedPath() {
        return Optional.of(injector).map(IInjector::getRejectedPath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    /**
     * Returns the folder where the messages are moved if they are to be retried
     */
    public final String getRetryPath() {
        return Optional.of(injector).map(IInjector::getRetryPath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    /**
     * Returns the folder where the messages are moved if they are to be archived
     */
    public final String getArchivePath() {
        return Optional.of(injector).map(IInjector::getArchivePath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    /**
     * Move file to the accepted folder
     */
    public final void acceptFile(File file) {
        boolean moved = file.renameTo(new File(getAcceptedPath(), file.getName()));
        if (!moved) {
            logService.warn(() -> "File {0} was not moved to the accepted path!", file.getName());
        }
    }

    /**
     * Move file to the rejected folder
     */
    public final void rejectFile(File file) {
        boolean moved = file.renameTo(new File(getRejectedPath(), file.getName()));
        if (!moved) {
            logService.warn(() -> "Flux {0} was not moved to the rejected path!", file.getName());
        }
    }

    private final class FileInjector extends AbstractInjector<M> {

        private final String engineUuid;

        /**
         * Builds an injector dedicated to managing flux of the given type
         */
        FileInjector(String engineUuid, String name, File baseWorkdir) {
            super(name, baseWorkdir.getAbsolutePath());

            this.engineUuid = engineUuid;
        }

        @Override
        protected void handleFlux(M flux) {
            AbstractImportAgent.this.handleFlux(flux, engineUuid);
        }

        @Override
        public M createFlux() {
            M flux = AbstractImportAgent.this.createFlux();
            flux.setName(super.getName());
            return flux;
        }
    }

    private final class SimpleAgent extends AbstractAgent {

        SimpleAgent(String name) {
            super(name);
        }

        @Override
        protected Agent getAgent() {
            return AbstractImportAgent.this.getAgent();
        }

        @Override
        public void work(Serializable message) {
            // this method cannot be called, the simple agent is not linked to a channel
        }
    }
}
