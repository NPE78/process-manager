package com.talanlabs.processmanager.messages.injector;

import com.talanlabs.processmanager.messages.gate.Gate;
import com.talanlabs.processmanager.messages.trigger.event.FileTriggerEvent;
import java.io.File;

public abstract class AbstractMsgInjector implements MessageInjector {

    private Gate gate;
    private File workDir;

    protected AbstractMsgInjector() {
    }

    protected AbstractMsgInjector(String rootPath) {
        workDir = new File(rootPath);
    }

    @Override
    public File getWorkDir() {
        return workDir;
    }

    void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public Gate getGate() {
        return gate;
    }

    public void setGate(Gate g) {
        this.gate = g;
    }

    public abstract Object inject(FileTriggerEvent evt);

    public final void manageResponse(String resp, File file2move) {
        if (resp != null) {
            if ("ok".equalsIgnoreCase(resp)) {
                this.gate.accept(file2move.getName());
            } else if ("nok".equalsIgnoreCase(resp)) {
                this.gate.reject(file2move.getName());
            } else if ("noanswer".equalsIgnoreCase(resp)) {
                this.gate.retry(file2move.getName());
            }
        } else {
            this.gate.retry(file2move.getName());
        }
    }
}
