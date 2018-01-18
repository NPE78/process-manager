package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.exceptions.AddonAlreadyBoundException;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;

public abstract class EngineAddon<V extends EngineAddon> implements Engine.IEngineAddon {

    private final LogService logService;

    private final Class<V> engineAddonClass;
    private final String engineUuid;

    public EngineAddon(Class<V> engineAddonClass, String engineUuid) {
        logService = LogManager.getLogService(getClass());

        this.engineAddonClass = engineAddonClass;
        this.engineUuid = engineUuid;
    }

    public final LogService getLogService() {
        return logService;
    }

    @Override
    public final Class<V> getAddonClass() {
        return engineAddonClass;
    }

    public final String getEngineUuid() {
        return engineUuid;
    }

    @SuppressWarnings("unchecked")
    protected final V registerAddon() {
        try {
            Engine engine = PM.getEngine(engineUuid);
            engine.addAddon(this);
        } catch (AddonAlreadyBoundException e) {
            logService.warn(() -> "Addon trigger engine has been bound twice", e);
            disconnectAddon();
            return (V) e.getEngineAddon();
        }
        return (V) this;
    }

    public abstract void disconnectAddon();

}
