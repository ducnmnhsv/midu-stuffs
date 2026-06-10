package com.techx.tradex.common.model.notification;

import lombok.Data;

@Data
public class AlarmNotificationData implements TemplateData {
    private String code;
    private double value;

    @Override
    public String getTemplate() {
        return "alarm_notification";
    }
}
