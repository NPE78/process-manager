package com.talanlabs.processmanager.messages.gate;

import com.talanlabs.processmanager.messages.injector.MessageInjector;
import java.io.File;

/**
 * A default file sys gate to monitor a folder, and dispatch files to accepted, rejected and archive directory.<br>
 * If a retryPeriod has been provided, the retry folder is also monitored
 */
public class DefaultFileSysGate extends AbstractFileSysGate {

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
