package com.difisoft.marketcollector.services.recover;

import lombok.Data;

@Data
public class Status {
    String code;
    boolean hasFile;
    boolean success;
    String failMessage;

    public Status(String code) {
        this.code = code;
        this.success = true;
        this.hasFile = true;
    }

    public Status(String code, boolean hasFile, String failMessage) {
        this.code = code;
        this.hasFile = hasFile;
        this.success = false;
        this.failMessage = failMessage;
    }
}
