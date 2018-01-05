package com.talanlabs.processmanager.messages.injector;

import com.talanlabs.processmanager.messages.gate.Gate;
import com.talanlabs.processmanager.messages.trigger.event.FileTriggerEvent;
import java.io.File;

public interface MessageInjector {

    Object inject(FileTriggerEvent evt);

    Gate getGate();

    void setGate(Gate g);

    File getWorkDir();

}
