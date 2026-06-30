package com.techx.tradex.notification.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private Long userId;
    private Date date;
    private String imgUrl;
    private String title;
    private String titleVi;
    private String content;
    private String contentVi;
    private String type;
    private Date updatedAt;
    private Date createdAt;
    private Date deletedAt;
    private Boolean isRead;
    private String subAccount;
    private Long followerId;
}
