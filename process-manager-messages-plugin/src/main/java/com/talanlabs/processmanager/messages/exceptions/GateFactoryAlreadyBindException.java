package com.talanlabs.processmanager.messages.exceptions;

public class GateFactoryAlreadyBindException extends RuntimeException {

    public GateFactoryAlreadyBindException(String engineUuid) {
        super("A gate factory is already bind to the engine " + engineUuid);
    }
}
