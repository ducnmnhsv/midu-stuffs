package com.techx.tradex.order.model.db;

import com.difisoft.model.constants.OcoOrderStatusEnum;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.requests.Headers;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.techx.tradex.order.model.OrderMatchNotify;
import com.techx.tradex.order.model.converters.HeaderConvert;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_oco_order")
@Data
@ToString(exclude = {"bullBearOrder"})
@EqualsAndHashCode(exclude = {"bullBearOrder"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class OcoOrder implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bull_bear_id", referencedColumnName = "id")
    private BullBearOrder bullBearOrder;
    private String code;
    private long quantity;
    private long matchQuantity = 0;
    private long unmatchQuantity;
    @Enumerated(EnumType.STRING)
    private SellBuyTypeEnum sellBuyType;
    private Double currentPrice = 0D;
    private Double profitPrice = 0D;
    private Double triggerLossPrice = 0D;
    private Double toler = 0D;
    @Enumerated(EnumType.STRING)
    private OcoOrderStatusEnum status;
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
    @OneToMany(mappedBy = "ocoOrder")
    private List<ProfitLossOrder> profitLossOrders;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;

    public static OcoOrder fromBullBearOrder(BullBearOrder bullBearOrder) {
        OcoOrder ocoOrder = new OcoOrder();
        ocoOrder.setBullBearOrder(bullBearOrder);
        ocoOrder.setCode(bullBearOrder.getCode());
        ocoOrder.setQuantity(bullBearOrder.getQuantity());
        ocoOrder.setUnmatchQuantity(bullBearOrder.getQuantity());
        ocoOrder.setMatchQuantity(0);
        ocoOrder.setStatus(OcoOrderStatusEnum.PENDING);
        ocoOrder.setSellBuyType(SellBuyTypeEnum.BUY.equals(bullBearOrder.getSellBuyType()) ? SellBuyTypeEnum.SELL : SellBuyTypeEnum.BUY);
        ocoOrder.setProfitPrice(bullBearOrder.getProfitPrice());
        ocoOrder.setTriggerLossPrice(bullBearOrder.getTriggerLossPrice());
        ocoOrder.setToler(bullBearOrder.getToler());
        ocoOrder.setAccountNumber(bullBearOrder.getAccountNumber());
        ocoOrder.setHeader(bullBearOrder.getHeader());
        ocoOrder.setSourceIp(bullBearOrder.getSourceIp());
        return ocoOrder;
    }

    public void updateByOrderMatch(OrderMatchNotify orderMatchNotify) {
        this.setMatchQuantity(orderMatchNotify.getMatchQuantity());
        this.setUnmatchQuantity(this.getQuantity() - orderMatchNotify.getMatchQuantity());
    }
}
