package com.talanlabs.processmanager.messages.model;

import java.io.Serializable;

public class SendInformation implements Serializable {

    private boolean valid;

    private String messageContent;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}
