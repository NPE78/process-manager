package com.talanlabs.processmanager.messages.gate;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.messages.exceptions.GateFactoryAlreadyBindException;
import com.talanlabs.processmanager.messages.injector.MessageInjector;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.IGateFactory;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GateFactory implements IGateFactory {

    public static final Engine.EnginePropertyKey<GateFactory> KEY = () -> GateFactory.class;

    private final LogService logService;

    private final List<Gate> gateList;

    /* package protected */ GateFactory() {
        super();

        logService = LogManager.getLogService(getClass());

        gateList = new ArrayList<>();
    }

    public static GateFactory register(String engineUuid) {
        Engine engine = ProcessManager.getEngine(engineUuid);
        synchronized (KEY) {
            if (engine.getProperty(KEY) != null) {
                throw new GateFactoryAlreadyBindException(engineUuid);
            }
            GateFactory gateFactory = new GateFactory();
            engine.setProperty(KEY, gateFactory);
            return gateFactory;
        }
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
