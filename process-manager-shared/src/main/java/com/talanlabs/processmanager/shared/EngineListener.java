package com.talanlabs.processmanager.shared;

import java.io.Serializable;

public interface EngineListener {

    void notifyHandle(String channelName, Object message);

    void notifyNewSlot(ChannelSlot slot);

    void notifySlotPlug(ChannelSlot slot);

    void notifySlotUnplug(ChannelSlot slot);

    void notifySlotBuffering(ChannelSlot slot, Serializable message);

    void notifySlotFlushing(ChannelSlot slot, Serializable message);

    void notifySlotTrashing(ChannelSlot slot, Serializable message);

}
