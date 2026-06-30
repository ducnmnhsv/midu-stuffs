package com.difisoft.nhsv.admin.service.criteria;

import com.difisoft.nhsv.admin.domain.enumeration.OrderSetTypeEnum;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.CopySubscriberHistory} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.CopySubscriberHistoryResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /copy-subscriber-histories?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopySubscriberHistoryCriteria implements Serializable, Criteria {

    /**
     * Class for filtering OrderSetTypeEnum
     */
    public static class OrderSetTypeEnumFilter extends Filter<OrderSetTypeEnum> {

        public OrderSetTypeEnumFilter() {}

        public OrderSetTypeEnumFilter(OrderSetTypeEnumFilter filter) {
            super(filter);
        }

        @Override
        public OrderSetTypeEnumFilter copy() {
            return new OrderSetTypeEnumFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter accountNumber;

    private StringFilter subNumber;

    private StringFilter userName;

    private DoubleFilter allocatedRatio;

    private OrderSetTypeEnumFilter orderSetType;

    private ZonedDateTimeFilter createdAt;

    private ZonedDateTimeFilter updatedAt;

    private LongFilter mlUserIdId;

    private Boolean distinct;

    public CopySubscriberHistoryCriteria() {}

    public CopySubscriberHistoryCriteria(CopySubscriberHistoryCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.accountNumber = other.accountNumber == null ? null : other.accountNumber.copy();
        this.subNumber = other.subNumber == null ? null : other.subNumber.copy();
        this.userName = other.userName == null ? null : other.userName.copy();
        this.allocatedRatio = other.allocatedRatio == null ? null : other.allocatedRatio.copy();
        this.orderSetType = other.orderSetType == null ? null : other.orderSetType.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.mlUserIdId = other.mlUserIdId == null ? null : other.mlUserIdId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CopySubscriberHistoryCriteria copy() {
        return new CopySubscriberHistoryCriteria(this);
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

    public StringFilter getAccountNumber() {
        return accountNumber;
    }

    public StringFilter accountNumber() {
        if (accountNumber == null) {
            accountNumber = new StringFilter();
        }
        return accountNumber;
    }

    public void setAccountNumber(StringFilter accountNumber) {
        this.accountNumber = accountNumber;
    }

    public StringFilter getSubNumber() {
        return subNumber;
    }

    public StringFilter subNumber() {
        if (subNumber == null) {
            subNumber = new StringFilter();
        }
        return subNumber;
    }

    public void setSubNumber(StringFilter subNumber) {
        this.subNumber = subNumber;
    }

    public StringFilter getUserName() {
        return userName;
    }

    public StringFilter userName() {
        if (userName == null) {
            userName = new StringFilter();
        }
        return userName;
    }

    public void setUserName(StringFilter userName) {
        this.userName = userName;
    }

    public DoubleFilter getAllocatedRatio() {
        return allocatedRatio;
    }

    public DoubleFilter allocatedRatio() {
        if (allocatedRatio == null) {
            allocatedRatio = new DoubleFilter();
        }
        return allocatedRatio;
    }

    public void setAllocatedRatio(DoubleFilter allocatedRatio) {
        this.allocatedRatio = allocatedRatio;
    }

    public OrderSetTypeEnumFilter getOrderSetType() {
        return orderSetType;
    }

    public OrderSetTypeEnumFilter orderSetType() {
        if (orderSetType == null) {
            orderSetType = new OrderSetTypeEnumFilter();
        }
        return orderSetType;
    }

    public void setOrderSetType(OrderSetTypeEnumFilter orderSetType) {
        this.orderSetType = orderSetType;
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

    public LongFilter getMlUserIdId() {
        return mlUserIdId;
    }

    public LongFilter mlUserIdId() {
        if (mlUserIdId == null) {
            mlUserIdId = new LongFilter();
        }
        return mlUserIdId;
    }

    public void setMlUserIdId(LongFilter mlUserIdId) {
        this.mlUserIdId = mlUserIdId;
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
        final CopySubscriberHistoryCriteria that = (CopySubscriberHistoryCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(accountNumber, that.accountNumber) &&
            Objects.equals(subNumber, that.subNumber) &&
            Objects.equals(userName, that.userName) &&
            Objects.equals(allocatedRatio, that.allocatedRatio) &&
            Objects.equals(orderSetType, that.orderSetType) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(mlUserIdId, that.mlUserIdId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            accountNumber,
            subNumber,
            userName,
            allocatedRatio,
            orderSetType,
            createdAt,
            updatedAt,
            mlUserIdId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopySubscriberHistoryCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (accountNumber != null ? "accountNumber=" + accountNumber + ", " : "") +
            (subNumber != null ? "subNumber=" + subNumber + ", " : "") +
            (userName != null ? "userName=" + userName + ", " : "") +
            (allocatedRatio != null ? "allocatedRatio=" + allocatedRatio + ", " : "") +
            (orderSetType != null ? "orderSetType=" + orderSetType + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (mlUserIdId != null ? "mlUserIdId=" + mlUserIdId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
