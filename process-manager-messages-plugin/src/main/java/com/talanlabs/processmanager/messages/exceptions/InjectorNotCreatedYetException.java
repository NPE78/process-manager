package com.talanlabs.processmanager.messages.exceptions;

public class InjectorNotCreatedYetException extends RuntimeException {

    public InjectorNotCreatedYetException() {
        super("The injector has not been created yet. Use AbstractFileAgent#register to fix this");
    }
}
