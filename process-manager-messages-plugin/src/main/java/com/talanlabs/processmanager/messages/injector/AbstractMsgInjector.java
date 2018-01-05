package com.talanlabs.processmanager.messages.injector;

import com.talanlabs.processmanager.messages.gate.Gate;
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

    @Override
    public Gate getGate() {
        return gate;
    }

    @Override
    public void setGate(Gate g) {
        this.gate = g;
    }
}
