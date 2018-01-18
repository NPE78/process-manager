package com.talanlabs.processmanager.messages.gate;

import com.talanlabs.processmanager.messages.injector.IInjector;
import com.talanlabs.processmanager.messages.model.FluxFolders;
import java.io.File;

/**
 * A default file sys gate to monitor a folder, and dispatch files to accepted, rejected and archive directory.<br>
 * If a retryPeriod has been provided, the retry folder is also monitored
 */
public class DefaultFileSysGate extends AbstractFileSysGate {

    public DefaultFileSysGate(String engineUuid, String name, FluxFolders gateFolders, long retryPeriod, IInjector injector) {
        super(engineUuid, name, gateFolders, retryPeriod, injector);
    }

    public DefaultFileSysGate(String engineUuid, String name, File rootDir, long retryPeriod, IInjector injector) {
        this(engineUuid, name, FluxFolders.from(rootDir), retryPeriod, injector);
    }
}
