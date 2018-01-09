package com.talanlabs.processmanager.shared.exceptions;

import com.talanlabs.processmanager.shared.Engine;

public class AddonAlreadyBoundException extends Exception {

    private final transient Engine.IEngineAddon engineAddon;

    public AddonAlreadyBoundException(String engineUuid, Engine.IEngineAddon engineAddon) {
        super("This addon " + engineAddon.getAddonClass().getSimpleName() + " has already been bound to the engine " + engineUuid);

        this.engineAddon = engineAddon;
    }

    public Engine.IEngineAddon getEngineAddon() {
        return engineAddon;
    }
}
