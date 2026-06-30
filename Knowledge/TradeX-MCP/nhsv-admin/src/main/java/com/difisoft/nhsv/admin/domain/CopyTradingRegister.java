package com.difisoft.nhsv.admin.domain;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
/**
 * A CopyTradingRegister.
 */
@Entity
@Table(name = "t_copy_trading_register")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyTradingRegister implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "account_number")
    private String accountNumber;
    @Column(name = "sub_account")
    private String subAccount;
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "status")
    private Boolean status;
    @Column(name = "create_at")
    private ZonedDateTime createAt;
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return this.id;
    }
    public CopyTradingRegister id(Long id) {
        this.setId(id);
        return this;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAccountNumber() {
        return this.accountNumber;
    }
    public CopyTradingRegister accountNumber(String accountNumber) {
        this.setAccountNumber(accountNumber);
        return this;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getSubAccount() {
        return this.subAccount;
    }
    public CopyTradingRegister subAccount(String subAccount) {
        this.setSubAccount(subAccount);
        return this;
    }
    public void setSubAccount(String subAccount) {
        this.subAccount = subAccount;
    }
    public String getCustomerName() {
        return this.customerName;
    }
    public CopyTradingRegister customerName(String customerName) {
        this.setCustomerName(customerName);
        return this;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public Boolean getStatus() {
        return this.status;
    }
    public CopyTradingRegister status(Boolean status) {
        this.setStatus(status);
        return this;
    }
    public void setStatus(Boolean status) {
        this.status = status;
    }
    public ZonedDateTime getCreateAt() {
        return this.createAt;
    }
    public CopyTradingRegister createAt(ZonedDateTime createAt) {
        this.setCreateAt(createAt);
        return this;
    }
    public void setCreateAt(ZonedDateTime createAt) {
        this.createAt = createAt;
    }
    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }
    public CopyTradingRegister updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }
    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyTradingRegister)) {
            return false;
        }
        return id != null && id.equals(((CopyTradingRegister) o).id);
    }
    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }
    // prettier-ignore
    @Override
    public String toString() {
        return "CopyTradingRegister{" +
            "id=" + getId() +
            ", accountNumber='" + getAccountNumber() + "'" +
            ", subAccount='" + getSubAccount() + "'" +
            ", customerName='" + getCustomerName() + "'" +
            ", status='" + getStatus() + "'" +
            ", createAt='" + getCreateAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
