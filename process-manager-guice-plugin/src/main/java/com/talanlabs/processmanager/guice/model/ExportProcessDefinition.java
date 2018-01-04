package com.talanlabs.processmanager.guice.model;

import com.talanlabs.processmanager.guice.trigger.dispatcher.IDispatcher;
import com.talanlabs.processmanager.messages.flux.IExportFlux;

public class ExportProcessDefinition<M extends IExportFlux> extends FluxProcessDefinition<M> {

    private Class<? extends IDispatcher<M>> dispatcherClass;

    public Class<? extends IDispatcher<M>> getDispatcherClass() {
        return dispatcherClass;
    }

    public void setDispatcherClass(Class<? extends IDispatcher<M>> dispatcherClass) {
        this.dispatcherClass = dispatcherClass;
    }
}
