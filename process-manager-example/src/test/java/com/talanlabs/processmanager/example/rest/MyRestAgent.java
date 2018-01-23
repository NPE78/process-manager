package com.talanlabs.processmanager.example.rest;

import com.talanlabs.processmanager.rest.agent.AbstractRestAgent;
import io.javalin.Context;

import java.io.Serializable;

class MyRestAgent extends AbstractRestAgent {

    private final boolean shouldLock;

    MyRestAgent(boolean shouldLock) {
        super("restAgent-" + shouldLock);

        this.shouldLock = shouldLock;
    }

    @Override
    protected void doWork(Serializable message, Context context) {
        context.result(String.valueOf(message));
    }

    @Override
    public int getMaxWaiting() {
        return 2;
    }

    @Override
    public Serializable extract(Context context) {
        return context.queryString();
    }

    @Override
    public boolean shouldLock() {
        return shouldLock;
    }
}
