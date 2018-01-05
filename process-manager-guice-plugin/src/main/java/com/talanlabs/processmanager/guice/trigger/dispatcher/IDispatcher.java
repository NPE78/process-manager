package com.talanlabs.processmanager.guice.trigger.dispatcher;

import com.talanlabs.processmanager.messages.flux.IExportFlux;

public interface IDispatcher<M extends IExportFlux> {

    /**
     * Send a message
     */
    void send(M exportFlux);

}
