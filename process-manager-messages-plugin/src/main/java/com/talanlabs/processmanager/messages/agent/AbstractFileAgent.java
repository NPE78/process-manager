package com.talanlabs.processmanager.messages.agent;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.messages.exceptions.InjectorNotCreatedYetException;
import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.messages.gate.GateFactory;
import com.talanlabs.processmanager.messages.injector.AbstractInjector;
import com.talanlabs.processmanager.messages.injector.IInjector;
import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.PluggableChannel;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.Serializable;
import java.util.Optional;

public abstract class AbstractFileAgent<M extends AbstractImportFlux> implements Agent {

    private final LogService logService;

    private final Class<M> fluxClass;
    private final String name;
    private FileInjector fileInjector;

    public AbstractFileAgent(Class<M> fluxClass) {
        super();

        logService = LogManager.getLogService(getClass());

        this.fluxClass = fluxClass;
        this.name = fluxClass.getSimpleName();
    }

    public String getName() {
        return name;
    }

    public final LogService getLogService() {
        return logService;
    }

    /**
     * Builds an injector which simply handles any file event back to this agent, using the channel
     */
    protected IInjector buildInjector(String engineUuid, File basePath) {
        fileInjector = new FileInjector(engineUuid, name, basePath);
        return fileInjector;

    }

    @SuppressWarnings("unchecked")
    @Override
    public final void work(Serializable message, String engineUuid) {
        if (fluxClass.isAssignableFrom(message.getClass())) {
            doWork((M) message, engineUuid);
        }
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
        Engine engine = ProcessManager.getEngine(engineUuid);
        GateFactory gateFactory = engine.getAddon(GateFactory.class)
                .orElseGet(() -> GateFactory.register(engineUuid));

        PluggableChannel pluggableChannel = new ProcessingChannel(name, maxWorking, this);
        gateFactory.buildGate(getName(), delay, buildInjector(engineUuid, basePath));
        engine.plugChannel(pluggableChannel);
    }

    protected abstract M createFlux();

    public final File getWorkDir() {
        return Optional.ofNullable(fileInjector).map(FileInjector::getWorkDir).orElseThrow(InjectorNotCreatedYetException::new);
    }

    public final String getAcceptedPath() {
        return Optional.ofNullable(fileInjector).map(FileInjector::getAcceptedPath).orElseThrow(InjectorNotCreatedYetException::new);
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

    public final String getRejectedPath() {
        return Optional.of(fileInjector).map(FileInjector::getRejectedPath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    public final String getRetryPath() {
        return Optional.of(fileInjector).map(FileInjector::getRetryPath).orElseThrow(InjectorNotCreatedYetException::new);
    }

    public final String getArchivePath() {
        return Optional.of(fileInjector).map(FileInjector::getArchivePath).orElseThrow(InjectorNotCreatedYetException::new);
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
            ProcessManager.getEngine(engineUuid).handle(super.getName(), flux);
        }

        @Override
        public M createFlux() {
            M flux = AbstractFileAgent.this.createFlux();
            flux.setName(name);
            return flux;
        }
    }
}
