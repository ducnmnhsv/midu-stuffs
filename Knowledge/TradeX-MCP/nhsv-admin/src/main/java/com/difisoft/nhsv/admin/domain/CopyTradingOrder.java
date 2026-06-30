package com.difisoft.nhsv.admin.domain;

import com.difisoft.nhsv.admin.domain.enumeration.ExchangeTypeEnum;
import com.difisoft.nhsv.admin.domain.enumeration.OrderTypeEnum;
import com.difisoft.nhsv.admin.domain.enumeration.SellBuyTypeEnum;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CopyTradingOrder.
 */
@Entity
@Table(name = "t_copy_trading_order")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyTradingOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "job_id", length = 255, nullable = false)
    private String jobId;

    @NotNull
    @Size(max = 255)
    @Column(name = "symbol", length = 255, nullable = false)
    private String symbol;

    @Column(name = "fee")
    private Double fee;

    @Column(name = "tax")
    private Double tax;

    @Column(name = "order_number")
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "sell_buy_type")
    private SellBuyTypeEnum sellBuyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type")
    private ExchangeTypeEnum exchangeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type")
    private OrderTypeEnum orderType;

    @Column(name = "order_quantity")
    private Long orderQuantity;

    @Column(name = "order_price")
    private Double orderPrice;

    @Column(name = "api_param")
    private String apiParam;

    @Column(name = "api_status_code")
    private String apiStatusCode;

    @Column(name = "api_error_message")
    private String apiErrorMessage;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @NotNull
    @Column(name = "copy_subscriber_id_id", nullable = false)
    private Long copySubscriberId;

    @NotNull
    @Column(name = "copy_portfolio_id_id", nullable = false)
    private Long copyPortfolioId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CopyTradingOrder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobId() {
        return this.jobId;
    }

    public CopyTradingOrder jobId(String jobId) {
        this.setJobId(jobId);
        return this;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public CopyTradingOrder symbol(String symbol) {
        this.setSymbol(symbol);
        return this;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getFee() {
        return this.fee;
    }

    public CopyTradingOrder fee(Double fee) {
        this.setFee(fee);
        return this;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public Double getTax() {
        return this.tax;
    }

    public CopyTradingOrder tax(Double tax) {
        this.setTax(tax);
        return this;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public CopyTradingOrder orderNumber(String orderNumber) {
        this.setOrderNumber(orderNumber);
        return this;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType;
    }

    public CopyTradingOrder sellBuyType(SellBuyTypeEnum sellBuyType) {
        this.setSellBuyType(sellBuyType);
        return this;
    }

    public void setSellBuyType(SellBuyTypeEnum sellBuyType) {
        this.sellBuyType = sellBuyType;
    }

    public ExchangeTypeEnum getExchangeType() {
        return this.exchangeType;
    }

    public CopyTradingOrder exchangeType(ExchangeTypeEnum exchangeType) {
        this.setExchangeType(exchangeType);
        return this;
    }

    public void setExchangeType(ExchangeTypeEnum exchangeType) {
        this.exchangeType = exchangeType;
    }

    public OrderTypeEnum getOrderType() {
        return this.orderType;
    }

    public CopyTradingOrder orderType(OrderTypeEnum orderType) {
        this.setOrderType(orderType);
        return this;
    }

    public void setOrderType(OrderTypeEnum orderType) {
        this.orderType = orderType;
    }

    public Long getOrderQuantity() {
        return this.orderQuantity;
    }

    public CopyTradingOrder orderQuantity(Long orderQuantity) {
        this.setOrderQuantity(orderQuantity);
        return this;
    }

    public void setOrderQuantity(Long orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public Double getOrderPrice() {
        return this.orderPrice;
    }

    public CopyTradingOrder orderPrice(Double orderPrice) {
        this.setOrderPrice(orderPrice);
        return this;
    }

    public void setOrderPrice(Double orderPrice) {
        this.orderPrice = orderPrice;
    }

    public String getApiParam() {
        return this.apiParam;
    }

    public CopyTradingOrder apiParam(String apiParam) {
        this.setApiParam(apiParam);
        return this;
    }

    public void setApiParam(String apiParam) {
        this.apiParam = apiParam;
    }

    public String getApiStatusCode() {
        return this.apiStatusCode;
    }

    public CopyTradingOrder apiStatusCode(String apiStatusCode) {
        this.setApiStatusCode(apiStatusCode);
        return this;
    }

    public void setApiStatusCode(String apiStatusCode) {
        this.apiStatusCode = apiStatusCode;
    }

    public String getApiErrorMessage() {
        return this.apiErrorMessage;
    }

    public CopyTradingOrder apiErrorMessage(String apiErrorMessage) {
        this.setApiErrorMessage(apiErrorMessage);
        return this;
    }

    public void setApiErrorMessage(String apiErrorMessage) {
        this.apiErrorMessage = apiErrorMessage;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public CopyTradingOrder createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public CopyTradingOrder updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCopySubscriberId() {
        return this.copySubscriberId;
    }

    public CopyTradingOrder copySubscriberId(Long copySubscriberId) {
        this.setCopySubscriberId(copySubscriberId);
        return this;
    }

    public void setCopySubscriberId(Long copySubscriberId) {
        this.copySubscriberId = copySubscriberId;
    }

    public Long getCopyPortfolioId() {
        return this.copyPortfolioId;
    }

    public CopyTradingOrder copyPortfolioId(Long copyPortfolioId) {
        this.setCopyPortfolioId(copyPortfolioId);
        return this;
    }

    public void setCopyPortfolioId(Long copyPortfolioId) {
        this.copyPortfolioId = copyPortfolioId;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyTradingOrder)) {
            return false;
        }
        return id != null && id.equals(((CopyTradingOrder) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyTradingOrder{" +
            "id=" + getId() +
            ", jobId='" + getJobId() + "'" +
            ", symbol='" + getSymbol() + "'" +
            ", fee=" + getFee() +
            ", tax=" + getTax() +
            ", orderNumber='" + getOrderNumber() + "'" +
            ", sellBuyType='" + getSellBuyType() + "'" +
            ", exchangeType='" + getExchangeType() + "'" +
            ", orderType='" + getOrderType() + "'" +
            ", orderQuantity=" + getOrderQuantity() +
            ", orderPrice=" + getOrderPrice() +
            ", apiParam='" + getApiParam() + "'" +
            ", apiStatusCode='" + getApiStatusCode() + "'" +
            ", apiErrorMessage='" + getApiErrorMessage() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", copySubscriberId=" + getCopySubscriberId() +
            ", copyPortfolioId=" + getCopyPortfolioId() +
            "}";
    }
}
