package com.talanlabs.processmanager.engine.handlereport;

import com.talanlabs.processmanager.shared.ChannelSlot;
import com.talanlabs.processmanager.shared.HandleReport;

public class BaseHandleReport implements HandleReport {

    private ChannelSlot slot;

    BaseHandleReport(ChannelSlot slot) {
        this.slot = slot;
    }

    public ChannelSlot getChannelSlot() {
        return slot;
    }
}
