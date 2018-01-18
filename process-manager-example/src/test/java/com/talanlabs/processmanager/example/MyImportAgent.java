package com.talanlabs.processmanager.example;

import com.talanlabs.processmanager.messages.agent.AbstractImportAgent;
import java.io.IOException;

public class MyImportAgent extends AbstractImportAgent<MyFlux> {

    MyImportAgent() {
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
                rejectFile(flux.getFile());
            } else {
                acceptFile(flux.getFile());
            }
        } catch (IOException e) {
            getLogService().warn(() -> "IOException", e);
        }
    }
}
