package com.techx.tradex.order.model.db;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.constants.TrailingOrderStatusEnum;
import com.difisoft.model.requests.Headers;
import com.techx.tradex.order.model.converters.HeaderConvert;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;

@Entity
@Table(name = "t_trailing_order")
@Data
public class TrailingOrder implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private Long quantity;

    @Enumerated(EnumType.STRING)
    private SellBuyTypeEnum sellBuyType;

    private Double trailingAmount;

    private Double limitOffset;

    private Double currentPrice;

    private Double stopPrice;

    @Enumerated(EnumType.STRING)
    private TrailingOrderStatusEnum status;


    private String username;
    private String accountNumber;
    private String subNumber;
    private String orderNumber;
    private String failReason;
    private String errorCode;
    private String bankCode;
    private String bankAccount;
    private String bankName;
    private String securitiesType;
    private Date orderedAt;
    private Date cancelledAt;
    private String cancelledBy;
    private String sourceIp;
    @Convert(converter = HeaderConvert.class)
    private Headers header;
    private String tradingAccSeq; // only for mas

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
