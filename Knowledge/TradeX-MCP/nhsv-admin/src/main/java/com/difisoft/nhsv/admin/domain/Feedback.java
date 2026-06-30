package com.difisoft.nhsv.admin.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;

import javax.persistence.*;

@Entity
@Table(name = "t_feedback")
@Data
public class Feedback implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "phoneno")
    private String phoneNo;

    private String message;

    @Column(name = "image_url")
    private String imageUrl;

    private ZonedDateTime createdAt;
}
