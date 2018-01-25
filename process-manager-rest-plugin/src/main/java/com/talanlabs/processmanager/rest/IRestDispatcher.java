package com.talanlabs.processmanager.rest;

import io.javalin.ApiBuilder;

public interface IRestDispatcher extends ApiBuilder.EndpointGroup {

    /**
     * The name is used to build the route (URL behind the REST agents)
     */
    String getName();

    /**
     * Deactivate this dispatcher definitely
     */
    void clear();

}
