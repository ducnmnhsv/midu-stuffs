package com.techx.tradex.notification.model;

import com.techx.tradex.notification.model.dto.NotificationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDataRequest {
    private String key;
    private Long userId;
    private List<NotificationDTO> notificationDTOS;
}