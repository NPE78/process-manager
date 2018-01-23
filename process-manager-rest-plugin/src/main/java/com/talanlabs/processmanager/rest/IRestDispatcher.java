package com.talanlabs.processmanager.rest;

import io.javalin.ApiBuilder;

public interface IRestDispatcher extends ApiBuilder.EndpointGroup {

    String getName();

    void clear();

}
