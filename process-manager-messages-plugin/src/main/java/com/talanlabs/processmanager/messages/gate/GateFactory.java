package com.talanlabs.processmanager.messages.gate;

import com.talanlabs.processmanager.messages.injector.MessageInjector;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GateFactory {

    private final LogService logService;

    private final List<Gate> gateList;

    public GateFactory() {
        super();

        logService = LogManager.getLogService(getClass());

        this.gateList = new ArrayList<>();
    }

    public Gate buildGate(String id, long delay, MessageInjector messageInjector) {
        Gate gate = new DefaultFileSysGate(id, messageInjector.getWorkDir(), delay, messageInjector);
        gateList.add(gate);
        logService
                .info(() -> "GATE INSTALLATION {0} SUCCESSFUL [{1}({2})][{3}]",
                        gate.getName(),
                        id,
                        messageInjector.getClass().getSimpleName(),
                        messageInjector.getWorkDir().getPath());
        return gate;
    }

    public void closeGates() {
        Iterator<Gate> iterator = gateList.iterator();
        while (iterator.hasNext()) {
            Gate gate = iterator.next();
            if (gate.isOpened()) {
                gate.close();
            }
            iterator.remove();
        }
    }
}
