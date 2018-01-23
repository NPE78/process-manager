package com.talanlabs.processmanager.example.rest;

import com.talanlabs.processmanager.rest.AbstractRestDispatcher;
import com.talanlabs.processmanager.rest.agent.IRestAgent;

class MyRestDispatcher extends AbstractRestDispatcher {

    private final IRestAgent agentGet;
    private final IRestAgent agentPost;

    MyRestDispatcher(IRestAgent agentGet, IRestAgent agentPost) {
        super("rest");

        this.agentGet = agentGet;
        this.agentPost = agentPost;
    }

    @Override
    protected IRestAgent agentGet() {
        return agentGet;
    }

    @Override
    protected IRestAgent agentPost() {
        return agentPost;
    }
}
