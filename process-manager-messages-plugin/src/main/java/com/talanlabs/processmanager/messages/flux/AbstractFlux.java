package com.talanlabs.processmanager.messages.flux;

import com.talanlabs.processmanager.messages.listener.IMessageListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractFlux extends Flux {

    private Map<String, Serializable> extras;

    private final transient List<IMessageListener> messageListenerList;

    private int retry;

    AbstractFlux() {
        super();

        this.messageListenerList = new ArrayList<>(0);
        this.retry = 0;
    }

    /**
     * Called by the RetryAgent
     */
    public boolean retry() {
        retry++;
        return retry < 6;
    }

    /**
     * Get the number of retries by the RetryAgent
     */
    public int getRetryNumber() {
        return retry;
    }

    public Map<String, Serializable> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, Serializable> extras) {
        this.extras = extras;
    }

    @Override
    public void addMessageListener(IMessageListener messageListener) {
        addMessageListener(-1, messageListener);
    }

    @Override
    public void addMessageListener(int index, IMessageListener messageListener) {
        if (retry > 0) {
            return;
        }
        if (index >= 0) {
            messageListenerList.add(index > messageListenerList.size() ? messageListenerList.size() : index, messageListener);
        } else {
            messageListenerList.add(messageListener);
        }
    }

    @Override
    public void fireMessageListener(IMessageListener.Status status) {
        if (messageListenerList != null && !messageListenerList.isEmpty()) {
            for (IMessageListener messageListener : messageListenerList) {
                messageListener.messageTreated(status);
            }
        }
    }
}
