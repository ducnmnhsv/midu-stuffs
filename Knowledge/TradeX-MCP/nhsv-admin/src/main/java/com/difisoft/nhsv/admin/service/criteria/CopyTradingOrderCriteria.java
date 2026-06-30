package com.difisoft.nhsv.admin.service.criteria;

import com.difisoft.nhsv.admin.domain.enumeration.ExchangeTypeEnum;
import com.difisoft.nhsv.admin.domain.enumeration.OrderTypeEnum;
import com.difisoft.nhsv.admin.domain.enumeration.SellBuyTypeEnum;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.CopyTradingOrder} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.CopyTradingOrderResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /copy-trading-orders?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyTradingOrderCriteria implements Serializable, Criteria {

    /**
     * Class for filtering SellBuyTypeEnum
     */
    public static class SellBuyTypeEnumFilter extends Filter<SellBuyTypeEnum> {

        public SellBuyTypeEnumFilter() {}

        public SellBuyTypeEnumFilter(SellBuyTypeEnumFilter filter) {
            super(filter);
        }

        @Override
        public SellBuyTypeEnumFilter copy() {
            return new SellBuyTypeEnumFilter(this);
        }
    }

    /**
     * Class for filtering ExchangeTypeEnum
     */
    public static class ExchangeTypeEnumFilter extends Filter<ExchangeTypeEnum> {

        public ExchangeTypeEnumFilter() {}

        public ExchangeTypeEnumFilter(ExchangeTypeEnumFilter filter) {
            super(filter);
        }

        @Override
        public ExchangeTypeEnumFilter copy() {
            return new ExchangeTypeEnumFilter(this);
        }
    }

    /**
     * Class for filtering OrderTypeEnum
     */
    public static class OrderTypeEnumFilter extends Filter<OrderTypeEnum> {

        public OrderTypeEnumFilter() {}

        public OrderTypeEnumFilter(OrderTypeEnumFilter filter) {
            super(filter);
        }

        @Override
        public OrderTypeEnumFilter copy() {
            return new OrderTypeEnumFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter jobId;

    private StringFilter symbol;

    private DoubleFilter fee;

    private DoubleFilter tax;

    private StringFilter orderNumber;

    private SellBuyTypeEnumFilter sellBuyType;

    private ExchangeTypeEnumFilter exchangeType;

    private OrderTypeEnumFilter orderType;

    private LongFilter orderQuantity;

    private DoubleFilter orderPrice;

    private StringFilter apiParam;

    private StringFilter apiStatusCode;

    private StringFilter apiErrorMessage;

    private ZonedDateTimeFilter createdAt;

    private ZonedDateTimeFilter updatedAt;

    private LongFilter copySubscriberId;

    private LongFilter copyPortfolioId;

    private Boolean distinct;

    public CopyTradingOrderCriteria() {}

    public CopyTradingOrderCriteria(CopyTradingOrderCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.jobId = other.jobId == null ? null : other.jobId.copy();
        this.symbol = other.symbol == null ? null : other.symbol.copy();
        this.fee = other.fee == null ? null : other.fee.copy();
        this.tax = other.tax == null ? null : other.tax.copy();
        this.orderNumber = other.orderNumber == null ? null : other.orderNumber.copy();
        this.sellBuyType = other.sellBuyType == null ? null : other.sellBuyType.copy();
        this.exchangeType = other.exchangeType == null ? null : other.exchangeType.copy();
        this.orderType = other.orderType == null ? null : other.orderType.copy();
        this.orderQuantity = other.orderQuantity == null ? null : other.orderQuantity.copy();
        this.orderPrice = other.orderPrice == null ? null : other.orderPrice.copy();
        this.apiParam = other.apiParam == null ? null : other.apiParam.copy();
        this.apiStatusCode = other.apiStatusCode == null ? null : other.apiStatusCode.copy();
        this.apiErrorMessage = other.apiErrorMessage == null ? null : other.apiErrorMessage.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.copySubscriberId = other.copySubscriberId == null ? null : other.copySubscriberId.copy();
        this.copyPortfolioId = other.copyPortfolioId == null ? null : other.copyPortfolioId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CopyTradingOrderCriteria copy() {
        return new CopyTradingOrderCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getJobId() {
        return jobId;
    }

    public StringFilter jobId() {
        if (jobId == null) {
            jobId = new StringFilter();
        }
        return jobId;
    }

    public void setJobId(StringFilter jobId) {
        this.jobId = jobId;
    }

    public StringFilter getSymbol() {
        return symbol;
    }

    public StringFilter symbol() {
        if (symbol == null) {
            symbol = new StringFilter();
        }
        return symbol;
    }

    public void setSymbol(StringFilter symbol) {
        this.symbol = symbol;
    }

    public DoubleFilter getFee() {
        return fee;
    }

    public DoubleFilter fee() {
        if (fee == null) {
            fee = new DoubleFilter();
        }
        return fee;
    }

    public void setFee(DoubleFilter fee) {
        this.fee = fee;
    }

    public DoubleFilter getTax() {
        return tax;
    }

    public DoubleFilter tax() {
        if (tax == null) {
            tax = new DoubleFilter();
        }
        return tax;
    }

    public void setTax(DoubleFilter tax) {
        this.tax = tax;
    }

    public StringFilter getOrderNumber() {
        return orderNumber;
    }

    public StringFilter orderNumber() {
        if (orderNumber == null) {
            orderNumber = new StringFilter();
        }
        return orderNumber;
    }

    public void setOrderNumber(StringFilter orderNumber) {
        this.orderNumber = orderNumber;
    }

    public SellBuyTypeEnumFilter getSellBuyType() {
        return sellBuyType;
    }

    public SellBuyTypeEnumFilter sellBuyType() {
        if (sellBuyType == null) {
            sellBuyType = new SellBuyTypeEnumFilter();
        }
        return sellBuyType;
    }

    public void setSellBuyType(SellBuyTypeEnumFilter sellBuyType) {
        this.sellBuyType = sellBuyType;
    }

    public ExchangeTypeEnumFilter getExchangeType() {
        return exchangeType;
    }

    public ExchangeTypeEnumFilter exchangeType() {
        if (exchangeType == null) {
            exchangeType = new ExchangeTypeEnumFilter();
        }
        return exchangeType;
    }

    public void setExchangeType(ExchangeTypeEnumFilter exchangeType) {
        this.exchangeType = exchangeType;
    }

    public OrderTypeEnumFilter getOrderType() {
        return orderType;
    }

    public OrderTypeEnumFilter orderType() {
        if (orderType == null) {
            orderType = new OrderTypeEnumFilter();
        }
        return orderType;
    }

    public void setOrderType(OrderTypeEnumFilter orderType) {
        this.orderType = orderType;
    }

    public LongFilter getOrderQuantity() {
        return orderQuantity;
    }

    public LongFilter orderQuantity() {
        if (orderQuantity == null) {
            orderQuantity = new LongFilter();
        }
        return orderQuantity;
    }

    public void setOrderQuantity(LongFilter orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public DoubleFilter getOrderPrice() {
        return orderPrice;
    }

    public DoubleFilter orderPrice() {
        if (orderPrice == null) {
            orderPrice = new DoubleFilter();
        }
        return orderPrice;
    }

    public void setOrderPrice(DoubleFilter orderPrice) {
        this.orderPrice = orderPrice;
    }

    public StringFilter getApiParam() {
        return apiParam;
    }

    public StringFilter apiParam() {
        if (apiParam == null) {
            apiParam = new StringFilter();
        }
        return apiParam;
    }

    public void setApiParam(StringFilter apiParam) {
        this.apiParam = apiParam;
    }

    public StringFilter getApiStatusCode() {
        return apiStatusCode;
    }

    public StringFilter apiStatusCode() {
        if (apiStatusCode == null) {
            apiStatusCode = new StringFilter();
        }
        return apiStatusCode;
    }

    public void setApiStatusCode(StringFilter apiStatusCode) {
        this.apiStatusCode = apiStatusCode;
    }

    public StringFilter getApiErrorMessage() {
        return apiErrorMessage;
    }

    public StringFilter apiErrorMessage() {
        if (apiErrorMessage == null) {
            apiErrorMessage = new StringFilter();
        }
        return apiErrorMessage;
    }

    public void setApiErrorMessage(StringFilter apiErrorMessage) {
        this.apiErrorMessage = apiErrorMessage;
    }

    public ZonedDateTimeFilter getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTimeFilter createdAt() {
        if (createdAt == null) {
            createdAt = new ZonedDateTimeFilter();
        }
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTimeFilter createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTimeFilter getUpdatedAt() {
        return updatedAt;
    }

    public ZonedDateTimeFilter updatedAt() {
        if (updatedAt == null) {
            updatedAt = new ZonedDateTimeFilter();
        }
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTimeFilter updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LongFilter getCopySubscriberId() {
        return copySubscriberId;
    }

    public LongFilter copySubscriberId() {
        if (copySubscriberId == null) {
            copySubscriberId = new LongFilter();
        }
        return copySubscriberId;
    }

    public void setCopySubscriberId(LongFilter copySubscriberId) {
        this.copySubscriberId = copySubscriberId;
    }

    public LongFilter getCopyPortfolioId() {
        return copyPortfolioId;
    }

    public LongFilter copyPortfolioId() {
        if (copyPortfolioId == null) {
            copyPortfolioId = new LongFilter();
        }
        return copyPortfolioId;
    }

    public void setCopyPortfolioId(LongFilter copyPortfolioId) {
        this.copyPortfolioId = copyPortfolioId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CopyTradingOrderCriteria that = (CopyTradingOrderCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(jobId, that.jobId) &&
            Objects.equals(symbol, that.symbol) &&
            Objects.equals(fee, that.fee) &&
            Objects.equals(tax, that.tax) &&
            Objects.equals(orderNumber, that.orderNumber) &&
            Objects.equals(sellBuyType, that.sellBuyType) &&
            Objects.equals(exchangeType, that.exchangeType) &&
            Objects.equals(orderType, that.orderType) &&
            Objects.equals(orderQuantity, that.orderQuantity) &&
            Objects.equals(orderPrice, that.orderPrice) &&
            Objects.equals(apiParam, that.apiParam) &&
            Objects.equals(apiStatusCode, that.apiStatusCode) &&
            Objects.equals(apiErrorMessage, that.apiErrorMessage) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(copySubscriberId, that.copySubscriberId) &&
            Objects.equals(copyPortfolioId, that.copyPortfolioId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            jobId,
            symbol,
            fee,
            tax,
            orderNumber,
            sellBuyType,
            exchangeType,
            orderType,
            orderQuantity,
            orderPrice,
            apiParam,
            apiStatusCode,
            apiErrorMessage,
            createdAt,
            updatedAt,
            copySubscriberId,
            copyPortfolioId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyTradingOrderCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (jobId != null ? "jobId=" + jobId + ", " : "") +
            (symbol != null ? "symbol=" + symbol + ", " : "") +
            (fee != null ? "fee=" + fee + ", " : "") +
            (tax != null ? "tax=" + tax + ", " : "") +
            (orderNumber != null ? "orderNumber=" + orderNumber + ", " : "") +
            (sellBuyType != null ? "sellBuyType=" + sellBuyType + ", " : "") +
            (exchangeType != null ? "exchangeType=" + exchangeType + ", " : "") +
            (orderType != null ? "orderType=" + orderType + ", " : "") +
            (orderQuantity != null ? "orderQuantity=" + orderQuantity + ", " : "") +
            (orderPrice != null ? "orderPrice=" + orderPrice + ", " : "") +
            (apiParam != null ? "apiParam=" + apiParam + ", " : "") +
            (apiStatusCode != null ? "apiStatusCode=" + apiStatusCode + ", " : "") +
            (apiErrorMessage != null ? "apiErrorMessage=" + apiErrorMessage + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (copySubscriberId != null ? "copySubscriberId=" + copySubscriberId + ", " : "") +
            (copyPortfolioId != null ? "copyPortfolioId=" + copyPortfolioId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
