package com.techx.tradex.order.model.db;

import com.difisoft.model.constants.PlOrderTypeEnum;
import com.difisoft.model.constants.ProfitLossOrderStatusEnum;
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

@Entity
@Table(name = "t_profit_loss_order")
@Data
@ToString(exclude = {"bullBearOrder"})
@EqualsAndHashCode(exclude = {"bullBearOrder"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ProfitLossOrder implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "oco_id", referencedColumnName = "id")
    private OcoOrder ocoOrder;
    @OneToOne
    @JoinColumn(name = "bull_bear_id", referencedColumnName = "id")
    private BullBearOrder bullBearOrder;
    private String code;
    private long quantity;
    private long matchQuantity;
    @Enumerated(EnumType.STRING)
    private SellBuyTypeEnum sellBuyType;
    private Double orderPrice = 0D;
    @Enumerated(EnumType.STRING)
    private ProfitLossOrderStatusEnum status;
    @Enumerated(EnumType.STRING)
    private PlOrderTypeEnum profitLossType;
    private String username;
    private String accountNumber;
    private String subNumber;
    private String tradingAccSeq; // only for mas
    private String orderNumber; // orderID
    private String orderGroupNumber; // orderGroupID
    private String failReason;
    private String bankCode;
    private String bankAccount;
    private String bankName;
    private String securitiesType;
    private Date orderedAt;
    private Date cancelledAt;
    private String cancelledBy;
    @Convert(converter = HeaderConvert.class)
    private Headers header;
    private String sourceIp;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;
    private long cancelledQuantity;

    public static ProfitLossOrder fromBullBearOrder(BullBearOrder bullBearOrder) {
        ProfitLossOrder plOrder = new ProfitLossOrder();
        plOrder.setBullBearOrder(bullBearOrder);
        plOrder.setCode(bullBearOrder.getCode());
        plOrder.setQuantity(bullBearOrder.getQuantity());
        plOrder.setStatus(ProfitLossOrderStatusEnum.SENDING);
        plOrder.setSellBuyType(bullBearOrder.getSellBuyType());
        plOrder.setOrderPrice(bullBearOrder.getOrderPrice());
        plOrder.setUsername(bullBearOrder.getUsername());
        plOrder.setSecuritiesType(bullBearOrder.getSecuritiesType());
        plOrder.setOrderedAt(new Date());
        plOrder.setAccountNumber(bullBearOrder.getAccountNumber());
        plOrder.setHeader(bullBearOrder.getHeader());
        plOrder.setProfitLossType(PlOrderTypeEnum.OPEN_POSITION);
        return plOrder;
    }

    public static ProfitLossOrder fromOcoOrder(OcoOrder ocoOrder, PlOrderTypeEnum plType) {
        ProfitLossOrder plOrder = new ProfitLossOrder();
        plOrder.setProfitLossType(plType);
        plOrder.setOcoOrder(ocoOrder);
        plOrder.setCode(ocoOrder.getCode());
        plOrder.setQuantity(ocoOrder.getQuantity() - ocoOrder.getMatchQuantity());
        plOrder.setSellBuyType(ocoOrder.getSellBuyType());
        plOrder.setUsername(ocoOrder.getUsername());
        plOrder.setSecuritiesType(ocoOrder.getSecuritiesType());
        plOrder.setOrderedAt(new Date());
        plOrder.setAccountNumber(ocoOrder.getAccountNumber());
        plOrder.setHeader(ocoOrder.getHeader());
        plOrder.setProfitLossType(plType);
        if (PlOrderTypeEnum.TAKE_PROFIT.equals(plType)) {
            plOrder.setOrderPrice(ocoOrder.getProfitPrice());
        } else if (PlOrderTypeEnum.CUT_LOSS.equals(plType)) {
            plOrder.setOrderPrice(SellBuyTypeEnum.SELL.equals(plOrder.getSellBuyType()) ? ocoOrder.getTriggerLossPrice() - ocoOrder.getToler() : ocoOrder.getTriggerLossPrice() + ocoOrder.getToler());
        }
        return plOrder;
    }

    public void updateByOrderMatch(OrderMatchNotify orderMatchNotify) {
        this.setMatchQuantity(orderMatchNotify.getMatchQuantity());
    }
}
