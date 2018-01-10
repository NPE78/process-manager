package com.talanlabs.processmanager.messages.gate;

import com.talanlabs.processmanager.engine.EngineAddon;
import com.talanlabs.processmanager.messages.injector.MessageInjector;
import com.talanlabs.processmanager.shared.IGateFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GateFactory extends EngineAddon<GateFactory> implements IGateFactory {

    private final List<Gate> gateList;

    /* package protected */ GateFactory(String engineUuid) {
        super(GateFactory.class, engineUuid);

        gateList = new ArrayList<>();
    }

    public List<Gate> getGateList() {
        return new ArrayList<>(gateList);
    }

    @Override
    public void disconnectAddon() {
        closeGates();
    }

    public static GateFactory register(String engineUuid) {
        return new GateFactory(engineUuid).registerAddon();
    }

    public Gate buildGate(String id, long delay, MessageInjector messageInjector) {
        Gate gate = new DefaultFileSysGate(getEngineUuid(), id, messageInjector.getWorkDir(), delay, messageInjector);
        gateList.add(gate);
        getLogService().info(() -> "GATE INSTALLATION {0} SUCCESSFUL [{1}({2})][{3}]",
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
