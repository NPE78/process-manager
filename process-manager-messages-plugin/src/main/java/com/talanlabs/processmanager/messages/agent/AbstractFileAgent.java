package com.talanlabs.processmanager.messages.agent;

import com.talanlabs.processmanager.engine.AbstractAgent;
import com.talanlabs.processmanager.engine.ProcessManager;
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
public abstract class AbstractFileAgent<M extends AbstractImportFlux> implements Agent {

    private final LogService logService;

    private final SimpleAgent simpleAgent;
    private final Class<M> fluxClass;
    private IInjector fileInjector;

    public AbstractFileAgent(Class<M> fluxClass) {
        super();

        this.simpleAgent = new SimpleAgent(fluxClass.getSimpleName());

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
        fileInjector = new FileInjector(engineUuid, getName(), basePath); // done twice with #register, in case buildInjector is called separately
        // we could check a file injector wasn't set yet
        return fileInjector;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void work(Serializable message, String engineUuid) {
        if (isConcerned(message)) {
            doWork((M) message, engineUuid);
        } else {
            logService.warn(() -> "Agent {0} received a message it could not understand: {1}", getName(), message != null ? message.getClass() : "null");
        }
    }

    private boolean isConcerned(Serializable message) {
        return message != null && fluxClass.isAssignableFrom(message.getClass());
    }

    public abstract void doWork(M flux, String engineUuid);

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

        Engine engine = ProcessManager.getEngine(engineUuid);
        GateFactory gateFactory = engine.getAddon(GateFactory.class)
                .orElseGet(() -> GateFactory.register(engineUuid));
        fileInjector = buildInjector(engineUuid, basePath);
        gateFactory.buildGate(getName(), delay, fileInjector);
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
        ProcessManager.getEngine(engineUuid).handle(getName(), flux);
    }

    protected Agent getAgent() {
        return this;
    }

    public final File getWorkDir() {
        return Optional.ofNullable(fileInjector).map(IInjector::getWorkDir).orElseThrow(InjectorNotCreatedYetException::new);
    }

    public final String getAcceptedPath() {
        return Optional.ofNullable(fileInjector).map(IInjector::getAcceptedPath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    public final String getRejectedPath() {
        return Optional.of(fileInjector).map(IInjector::getRejectedPath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    public final String getRetryPath() {
        return Optional.of(fileInjector).map(IInjector::getRetryPath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    public final String getArchivePath() {
        return Optional.of(fileInjector).map(IInjector::getArchivePath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    public final void acceptFlux(M flux) {
        boolean moved = flux.getFile().renameTo(new File(getAcceptedPath(), flux.getFilename()));
        if (!moved) {
            logService.warn(() -> "Flux {0} was not moved to the accepted path!", flux.getFilename());
        }
    }

    public final void rejectFlux(M flux) {
        boolean moved = flux.getFile().renameTo(new File(getRejectedPath(), flux.getFilename()));
        if (!moved) {
            logService.warn(() -> "Flux {0} was not moved to the rejected path!", flux.getFilename());
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
            AbstractFileAgent.this.handleFlux(flux, engineUuid);
        }

        @Override
        public M createFlux() {
            M flux = AbstractFileAgent.this.createFlux();
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
            return AbstractFileAgent.this.getAgent();
        }

        @Override
        public void work(Serializable message, String engineUuid) {
            // this method cannot be called, the simple agent is not linked to a channel
        }
    }
}
