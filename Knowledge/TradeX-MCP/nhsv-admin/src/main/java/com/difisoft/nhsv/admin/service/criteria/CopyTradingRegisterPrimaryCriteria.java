package com.difisoft.nhsv.admin.service.criteria;

import com.difisoft.nhsv.admin.domain.enumeration.ActionEnum;
import lombok.Data;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.context.annotation.Primary;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

import java.io.Serializable;
import java.util.Objects;

@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
@Primary
@Data
public class CopyTradingRegisterPrimaryCriteria implements Serializable, Criteria {

    public static class StatusEnumFilter extends Filter<Boolean> {

        public StatusEnumFilter() {}

        public StatusEnumFilter(StatusEnumFilter filter) {
            super(filter);
        }

        @Override
        public StatusEnumFilter copy() {
            return new StatusEnumFilter(this);
        }
    }

    public static class ActionEnumFilter extends Filter<ActionEnum> {

        public ActionEnumFilter() {}

        public ActionEnumFilter(ActionEnumFilter filter) {
            super(filter);
        }

        @Override
        public ActionEnumFilter copy() {
            return new ActionEnumFilter(this);
        }
    }
    private static final long serialVersionUID = 1L;
    private LongFilter id;
    private StringFilter accountNumber;
    private StringFilter subAccount;
    private StringFilter customerName;
    private BooleanFilter status;
    private ZonedDateTimeFilter createAt;
    private ZonedDateTimeFilter updatedAt;
    private Boolean distinct;
    public CopyTradingRegisterPrimaryCriteria() {}
    public CopyTradingRegisterPrimaryCriteria(CopyTradingRegisterPrimaryCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.accountNumber = other.accountNumber == null ? null : other.accountNumber.copy();
        this.subAccount = other.subAccount == null ? null : other.subAccount.copy();
        this.customerName = other.customerName == null ? null : other.customerName.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.createAt = other.createAt == null ? null : other.createAt.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.distinct = other.distinct;
    }
    @Override
    public CopyTradingRegisterPrimaryCriteria copy() {
        return new CopyTradingRegisterPrimaryCriteria(this);
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
    public StringFilter getSubAccount() {
        return subAccount;
    }
    public StringFilter subAccount() {
        if (subAccount == null) {
            subAccount = new StringFilter();
        }
        return subAccount;
    }
    public void setSubAccount(StringFilter subAccount) {
        this.subAccount = subAccount;
    }
    public StringFilter getCustomerName() {
        return customerName;
    }
    public StringFilter customerName() {
        if (customerName == null) {
            customerName = new StringFilter();
        }
        return customerName;
    }
    public void setCustomerName(StringFilter customerName) {
        this.customerName = customerName;
    }

    public BooleanFilter getStatus() {
        return status;
    }

    public BooleanFilter status() {
        if (status == null) {
            status = new BooleanFilter();
        }
        return status;
    }
    public void setStatus(BooleanFilter status) {
        this.status = status;
    }
    public ZonedDateTimeFilter getCreateAt() {
        return createAt;
    }
    public ZonedDateTimeFilter createAt() {
        if (createAt == null) {
            createAt = new ZonedDateTimeFilter();
        }
        return createAt;
    }
    public void setCreateAt(ZonedDateTimeFilter createAt) {
        this.createAt = createAt;
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
        final CopyTradingRegisterPrimaryCriteria that = (CopyTradingRegisterPrimaryCriteria) o;
        return (
            Objects.equals(id, that.id) &&
                Objects.equals(accountNumber, that.accountNumber) &&
                Objects.equals(subAccount, that.subAccount) &&
                Objects.equals(customerName, that.customerName) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createAt, that.createAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(distinct, that.distinct)
        );
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber, subAccount, customerName, status, createAt, updatedAt, distinct);
    }
    // prettier-ignore
    @Override
    public String toString() {
        return "CopyTradingRegisterCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (accountNumber != null ? "accountNumber=" + accountNumber + ", " : "") +
            (subAccount != null ? "subAccount=" + subAccount + ", " : "") +
            (customerName != null ? "customerName=" + customerName + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (createAt != null ? "createAt=" + createAt + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
