package com.techx.tradex.notification.model.db;

import lombok.Data;

import javax.persistence.*;


@Entity
@Data
@Table(name = "t_notification_list")
public class NotificationList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String type;
    private String message_type;
    private String to;
    private String cc;
    private String bcc;
}
