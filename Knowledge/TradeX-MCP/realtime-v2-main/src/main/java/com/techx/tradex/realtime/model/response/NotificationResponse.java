package com.techx.tradex.realtime.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private List<Long> notificationIds;
    private String message;
    private Boolean isNotificationExists;
}
