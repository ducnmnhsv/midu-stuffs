package com.difisoft.nhsv.admin.domain;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.context.annotation.Primary;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "t_stock_event")
@Primary
public class StockEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    private String id;

    private String code;

    private String type;

    private ZonedDateTime effectiveDate;

    private ZonedDateTime expiredDate;

    private ZonedDateTime settlementDate;

    private String eventNote;

    private Double rate;

    private Boolean isAdjusted = false;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;
}
