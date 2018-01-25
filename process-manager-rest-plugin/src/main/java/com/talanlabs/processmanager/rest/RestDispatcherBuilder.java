package com.talanlabs.processmanager.rest;

import com.talanlabs.processmanager.rest.agent.IRestAgent;

public class RestDispatcherBuilder {

    private IRestAgent agentGet;
    private IRestAgent agentPost;
    private IRestAgent agentPut;
    private IRestAgent agentPatch;
    private IRestAgent agentDelete;
    private Long timeout;

    public RestDispatcherBuilder timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public RestDispatcherBuilder get(IRestAgent agentGet) {
        this.agentGet = agentGet;
        return this;
    }

    public RestDispatcherBuilder post(IRestAgent agentPost) {
        this.agentPost = agentPost;
        return this;
    }

    public RestDispatcherBuilder put(IRestAgent agentPut) {
        this.agentPut = agentPut;
        return this;
    }

    public RestDispatcherBuilder patch(IRestAgent agentPatch) {
        this.agentPatch = agentPatch;
        return this;
    }

    public RestDispatcherBuilder delete(IRestAgent agentDelete) {
        this.agentDelete = agentDelete;
        return this;
    }

    public IRestDispatcher build(String name) {
        RestDispatcher restDispatcher = new RestDispatcher(name);
        if (this.timeout != null) {
            restDispatcher.setTimeout(this.timeout);
        }
        restDispatcher.setAgentGet(agentGet);
        restDispatcher.setAgentPost(agentPost);
        restDispatcher.setAgentPut(agentPut);
        restDispatcher.setAgentPatch(agentPatch);
        restDispatcher.setAgentDelete(agentDelete);
        return restDispatcher;
    }

    private static class RestDispatcher extends AbstractRestDispatcher {

        private RestDispatcher(String name) {
            super(name);
        }
    }
}
