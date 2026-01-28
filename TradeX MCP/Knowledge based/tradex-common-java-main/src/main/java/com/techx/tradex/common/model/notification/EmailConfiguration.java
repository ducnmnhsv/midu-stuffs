package com.techx.tradex.common.model.notification;

import lombok.Data;

import java.util.List;

@Data
public class EmailConfiguration implements Configuration {
    private List<String> toList;
    private List<String> bccList;
    private List<String> ccList;
    private String from;
    private String subject;

    @Override
    public MethodEnum getMethod() {
        return MethodEnum.EMAIL;
    }
}
