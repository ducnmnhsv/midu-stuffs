package com.difisoft.nhsv.admin.service.dto;

import com.difisoft.nhsv.admin.domain.enumeration.ExchangeTypeEnum;
import com.difisoft.nhsv.admin.domain.enumeration.OrderTypeEnum;
import com.difisoft.nhsv.admin.domain.enumeration.SellBuyTypeEnum;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopyTradingOrder} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyTradingOrderDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String jobId;

    @NotNull
    @Size(max = 255)
    private String symbol;

    private Double fee;

    private Double tax;

    private String orderNumber;

    private SellBuyTypeEnum sellBuyType;

    private ExchangeTypeEnum exchangeType;

    private OrderTypeEnum orderType;

    private Long orderQuantity;

    private Double orderPrice;

    private String apiParam;

    private String apiStatusCode;

    private String apiErrorMessage;

    @NotNull
    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    @NotNull
    private Long copySubscriberId;

    @NotNull
    private Long copyPortfolioId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public SellBuyTypeEnum getSellBuyType() {
        return sellBuyType;
    }

    public void setSellBuyType(SellBuyTypeEnum sellBuyType) {
        this.sellBuyType = sellBuyType;
    }

    public ExchangeTypeEnum getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeTypeEnum exchangeType) {
        this.exchangeType = exchangeType;
    }

    public OrderTypeEnum getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderTypeEnum orderType) {
        this.orderType = orderType;
    }

    public Long getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(Long orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public Double getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(Double orderPrice) {
        this.orderPrice = orderPrice;
    }

    public String getApiParam() {
        return apiParam;
    }

    public void setApiParam(String apiParam) {
        this.apiParam = apiParam;
    }

    public String getApiStatusCode() {
        return apiStatusCode;
    }

    public void setApiStatusCode(String apiStatusCode) {
        this.apiStatusCode = apiStatusCode;
    }

    public String getApiErrorMessage() {
        return apiErrorMessage;
    }

    public void setApiErrorMessage(String apiErrorMessage) {
        this.apiErrorMessage = apiErrorMessage;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCopySubscriberId() {
        return copySubscriberId;
    }

    public void setCopySubscriberId(Long copySubscriberId) {
        this.copySubscriberId = copySubscriberId;
    }

    public Long getCopyPortfolioId() {
        return copyPortfolioId;
    }

    public void setCopyPortfolioId(Long copyPortfolioId) {
        this.copyPortfolioId = copyPortfolioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyTradingOrderDTO)) {
            return false;
        }

        CopyTradingOrderDTO copyTradingOrderDTO = (CopyTradingOrderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copyTradingOrderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyTradingOrderDTO{" +
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
