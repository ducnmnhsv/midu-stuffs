package com.difisoft.nhsv.admin.service.criteria;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;
/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.CopyTradingRegister} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.CopyTradingRegisterResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /copy-trading-registers?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyTradingRegisterCriteria implements Serializable, Criteria {
    private static final long serialVersionUID = 1L;
    private LongFilter id;
    private StringFilter accountNumber;
    private StringFilter subAccount;
    private StringFilter customerName;
    private BooleanFilter status;
    private ZonedDateTimeFilter createAt;
    private ZonedDateTimeFilter updatedAt;
    private Boolean distinct;
    public CopyTradingRegisterCriteria() {}
    public CopyTradingRegisterCriteria(CopyTradingRegisterCriteria other) {
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
    public CopyTradingRegisterCriteria copy() {
        return new CopyTradingRegisterCriteria(this);
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
        final CopyTradingRegisterCriteria that = (CopyTradingRegisterCriteria) o;
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
