package com.talanlabs.processmanager.guice.model;

import com.talanlabs.processmanager.messages.flux.IImportFlux;
import com.talanlabs.processmanager.messages.injector.IInjector;

public class ImportProcessDefinition<M extends IImportFlux> extends FluxProcessDefinition<M> {

    private Class<? extends IInjector> injectorClass;

    public Class<? extends IInjector> getInjectorClass() {
        return injectorClass;
    }

    public void setInjectorClass(Class<? extends IInjector> injectorClass) {
        this.injectorClass = injectorClass;
    }
}
