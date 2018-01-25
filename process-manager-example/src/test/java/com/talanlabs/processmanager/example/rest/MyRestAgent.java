package com.talanlabs.processmanager.example.rest;

import com.talanlabs.processmanager.rest.agent.AbstractRestAgent;
import io.javalin.Context;
import io.javalin.HaltException;
import org.eclipse.jetty.http.HttpStatus;

import java.io.Serializable;

class MyRestAgent extends AbstractRestAgent {

    private final boolean shouldLock;

    MyRestAgent(boolean shouldLock) {
        super("restAgent-" + shouldLock);

        this.shouldLock = shouldLock;
    }

    @Override
    protected void doWork(Serializable message, Context context) {
        String input = (String) message;
        context.result(mapInput(input));
    }

    private String mapInput(String input) {
        switch (input) {
            case "hello":
                return "hi there!";
            case "foo":
                return "bar";
            case "teapot":
                throw new HaltException(HttpStatus.IM_A_TEAPOT_418);
            default:
                throw new RuntimeException("test");
        }
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
