package com.talanlabs.processmanager.engine.handlereport;

import com.talanlabs.processmanager.shared.ChannelSlot;
import com.talanlabs.processmanager.shared.LocalHandleReport;

public class BaseLocalHandleReport extends BaseHandleReport implements LocalHandleReport {

    private Runnable runnable;

    public BaseLocalHandleReport(ChannelSlot slot, Runnable runnable) {
        super(slot);
        this.runnable = runnable;
    }

    @Override
    public Runnable getRunnable() {
        return runnable;
    }
}
