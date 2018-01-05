package com.talanlabs.processmanager.messages.trigger.event;

import com.talanlabs.processmanager.messages.trigger.api.Trigger;
import com.talanlabs.processmanager.messages.trigger.api.TriggerEvent;

public class BaseTriggerEvent<A> implements TriggerEvent {

    private A attachment;
    private long tt;
    private Trigger source;

    public BaseTriggerEvent(Trigger source, A attachment) {
        tt = System.currentTimeMillis();
        this.attachment = attachment;
        this.source = source;
    }

    @Override
    public long getTimeStamp() {
        return tt;
    }

    @Override
    public Trigger getSource() {
        return source;
    }

    @Override
    public A getAttachment() {
        return attachment;
    }

    @Override
    public String toString() {
        return "TRIGGER_EVENT (from " + getSource().getId() + ") " + (attachment != null ? attachment.toString() : "no attachment");
    }
}
