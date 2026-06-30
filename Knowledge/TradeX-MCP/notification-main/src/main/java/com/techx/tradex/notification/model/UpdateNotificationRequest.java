package com.techx.tradex.notification.model;

import java.util.Date;

import lombok.Data;

@Data
public class UpdateNotificationRequest {
	private String title;
    private String titleVi;
    private String content;
    private String contentVi;
    private String type;
    private Date date;
    private String kisUserName;
    private Boolean isRead;
} 