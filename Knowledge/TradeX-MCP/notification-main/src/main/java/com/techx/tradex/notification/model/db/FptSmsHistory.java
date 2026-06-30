package com.techx.tradex.notification.model.db;

import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;


@Entity
@Data
@Table(name = "t_fpt_sms_history")
public class FptSmsHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String brand_name;
    private String telco;
    private String phone_number;
    private String message_type;
    private String content;
    private String date;
    private Integer qty;
    private String status;
    private ZonedDateTime created_at;
    private ZonedDateTime updated_at;
    private String fail_reason;
}
