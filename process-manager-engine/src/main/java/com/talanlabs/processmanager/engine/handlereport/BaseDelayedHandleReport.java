package com.talanlabs.processmanager.engine.handlereport;

import com.talanlabs.processmanager.shared.ChannelSlot;
import com.talanlabs.processmanager.shared.DelayedHandleReport;

public class BaseDelayedHandleReport extends BaseHandleReport implements DelayedHandleReport {

    public BaseDelayedHandleReport(String sid, ChannelSlot slot) {
        super(slot);
    }
}
