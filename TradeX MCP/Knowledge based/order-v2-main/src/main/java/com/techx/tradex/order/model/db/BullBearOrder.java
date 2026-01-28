package com.techx.tradex.order.model.db;

import com.difisoft.model.constants.BullBearOrderStatusEnum;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.requests.Headers;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.techx.tradex.order.model.OrderMatchNotify;
import com.techx.tradex.order.model.converters.HeaderConvert;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_bull_bear_order")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class BullBearOrder implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private long quantity;
    private long matchQuantity;
    @Enumerated(EnumType.STRING)
    private SellBuyTypeEnum sellBuyType;
    private Double orderPrice = 0D;
    private Double profitPrice = 0D;
    private Double triggerLossPrice = 0D;
    private Double toler = 0D;
    @Enumerated(EnumType.STRING)
    private BullBearOrderStatusEnum status;
    private String username;
    private String accountNumber;
    private String subNumber;
    private String orderNumber;
    private String failReason;
    private String securitiesType;
    private Date orderedAt;
    private Date cancelledAt;
    private String cancelledBy;
    @Convert(converter = HeaderConvert.class)
    private Headers header;
    private String sourceIp;
    private String tradingAccSeq; // only for mas
    @OneToOne(mappedBy = "bullBearOrder")
    private OcoOrder ocoOrder;
    @OneToOne(mappedBy = "bullBearOrder")
    private ProfitLossOrder profitLossOrder;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;

    public void updateByOrderMatch(OrderMatchNotify orderMatchNotify) {
        this.setMatchQuantity(orderMatchNotify.getMatchQuantity());
    }
}
