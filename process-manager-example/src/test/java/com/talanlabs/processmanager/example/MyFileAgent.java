package com.talanlabs.processmanager.example;

import com.talanlabs.processmanager.messages.agent.AbstractFileAgent;
import java.io.IOException;

public class MyFileAgent extends AbstractFileAgent<MyFlux> {

    MyFileAgent() {
        super(MyFlux.class);
    }

    @Override
    protected MyFlux createFlux() {
        return new MyFlux();
    }

    @Override
    public void doWork(MyFlux flux, String engineUuid) {
        try {
            if ("invalid content".equals(flux.getContent())) {
                rejectFlux(flux);
            } else {
                acceptFlux(flux);
            }
        } catch (IOException e) {
            getLogService().warn(() -> "IOException", e);
        }
    }
}
