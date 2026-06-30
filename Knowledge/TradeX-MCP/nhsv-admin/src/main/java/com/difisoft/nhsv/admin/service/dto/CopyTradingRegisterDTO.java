package com.difisoft.nhsv.admin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopyTradingRegister} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyTradingRegisterDTO implements Serializable {
    private Long id;
    private String accountNumber;
    private String subAccount;
    private String customerName;
    private Boolean status;
    private ZonedDateTime createAt;
    private ZonedDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(String subAccount) {
        this.subAccount = subAccount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public ZonedDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(ZonedDateTime createAt) {
        this.createAt = createAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyTradingRegisterDTO)) {
            return false;
        }
        CopyTradingRegisterDTO copyTradingRegisterDTO = (CopyTradingRegisterDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copyTradingRegisterDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyTradingRegisterDTO{" +
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
