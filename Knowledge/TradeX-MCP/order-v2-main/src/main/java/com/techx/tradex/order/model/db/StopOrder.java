package com.techx.tradex.order.model.db;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.constants.StopOrderStatusEnum;
import com.difisoft.model.constants.StopOrderTypeEnum;
import com.difisoft.model.requests.Headers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.techx.tradex.order.model.converters.HeaderConvert;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "t_stop_order")
@Data
public class StopOrder implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private Long quantity;

    @Enumerated(EnumType.STRING)
    private SellBuyTypeEnum sellBuyType;

    private Double stopPrice = 0D;

    private Long stopVolume = 0L;

    private Double orderPrice;

    @Enumerated(EnumType.STRING)
    private StopOrderTypeEnum orderType = StopOrderTypeEnum.STOP;

    @Enumerated(EnumType.STRING)
    private StopOrderStatusEnum status;
    private ZonedDateTime fromDate;
    private ZonedDateTime toDate;

    private String username;
    private String accountNumber;
    private String subNumber;
    private String orderNumber;
    private String failReason;
    private String securitiesType;
    private ZonedDateTime orderedAt;
    private ZonedDateTime cancelledAt;
    private String cancelledBy;
    @Convert(converter = HeaderConvert.class)
    private Headers header;
    private String sourceIp;
    private String tradingAccSeq; // only for ttl
    private String macAddress;
    private String deviceUniqueId;

    private String bankCode; // only for lotte
    private String bankAccount; // only for lotte
    private String bankName; // only for lotte


    @CreationTimestamp
    private ZonedDateTime createdAt;
    @UpdateTimestamp
    private ZonedDateTime updatedAt;
    private String remark;

    @Transient
    @JsonIgnore
    public String getShortDescription() {
        return String.format("%d-%s-%f", this.id, this.code, this.stopPrice);
    }
}
