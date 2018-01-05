package com.talanlabs.processmanager.messages.gate;

import com.talanlabs.processmanager.messages.injector.MessageInjector;
import java.io.File;

class DefaultFileSysGate extends AbstractFileSysGate {

    public DefaultFileSysGate(String name, GateFolders gateFolders, long retryPeriod, MessageInjector injector) {
        super(name, gateFolders, retryPeriod, injector);
        super.init();
    }

    public DefaultFileSysGate(String name, File rootDir, long retryPeriod, MessageInjector injector) {
        this(name, new GateFolders(rootDir,
                new File(rootDir + "/accepted"),
                new File(rootDir + "/rejected"),
                new File(rootDir + "/retry"),
                new File(rootDir + "/archive")),
                retryPeriod, injector);
    }
}
