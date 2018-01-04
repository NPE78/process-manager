package com.talanlabs.processmanager.shared;

import java.io.Serializable;

public interface Agent {

    void work(Serializable message, String engineUuid);

}
