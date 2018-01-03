package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import com.talanlabs.processmanager.shared.ChannelSlot;
import com.talanlabs.processmanager.shared.EngineListener;
import java.io.Serializable;

public class DefaultEngineListener implements EngineListener {

    private final LogService logService;

    public DefaultEngineListener() {
        this.logService = LogManager.getLogService(DefaultEngineListener.class);
    }

    @Override
    public void notifyHandle(String channelName, Object message) {
    }

    @Override
    public void notifyNewSlot(ChannelSlot slot) {
    }

    @Override
    public void notifySlotPlug(ChannelSlot slot) {
    }

    @Override
    public void notifySlotUnplug(ChannelSlot slot) {
    }

    @Override
    public void notifySlotBuffering(ChannelSlot slot, Serializable message) {
    }

    @Override
    public void notifySlotFlushing(ChannelSlot slot, Serializable message) {
    }

    @Override
    public void notifySlotTrashing(ChannelSlot slot, Serializable message) {
        logService.warn(() -> "A message has been thrown to the trash for channel " + slot.getName());
    }
}
