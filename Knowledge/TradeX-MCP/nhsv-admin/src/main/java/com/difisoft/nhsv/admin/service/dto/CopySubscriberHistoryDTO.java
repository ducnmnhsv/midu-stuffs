package com.difisoft.nhsv.admin.service.dto;

import com.difisoft.nhsv.admin.domain.enumeration.OrderSetTypeEnum;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopySubscriberHistory} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopySubscriberHistoryDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String accountNumber;

    @NotNull
    @Size(max = 255)
    private String subNumber;

    @NotNull
    @Size(max = 255)
    private String userName;

    @NotNull
    private Double allocatedRatio;

    private OrderSetTypeEnum orderSetType;

    @NotNull
    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    private UserDTO mlUserId;

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

    public String getSubNumber() {
        return subNumber;
    }

    public void setSubNumber(String subNumber) {
        this.subNumber = subNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Double getAllocatedRatio() {
        return allocatedRatio;
    }

    public void setAllocatedRatio(Double allocatedRatio) {
        this.allocatedRatio = allocatedRatio;
    }

    public OrderSetTypeEnum getOrderSetType() {
        return orderSetType;
    }

    public void setOrderSetType(OrderSetTypeEnum orderSetType) {
        this.orderSetType = orderSetType;
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

    public UserDTO getMlUserId() {
        return mlUserId;
    }

    public void setMlUserId(UserDTO mlUserId) {
        this.mlUserId = mlUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopySubscriberHistoryDTO)) {
            return false;
        }

        CopySubscriberHistoryDTO copySubscriberHistoryDTO = (CopySubscriberHistoryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copySubscriberHistoryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopySubscriberHistoryDTO{" +
            "id=" + getId() +
            ", accountNumber='" + getAccountNumber() + "'" +
            ", subNumber='" + getSubNumber() + "'" +
            ", userName='" + getUserName() + "'" +
            ", allocatedRatio=" + getAllocatedRatio() +
            ", orderSetType='" + getOrderSetType() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", mlUserId=" + getMlUserId() +
            "}";
    }
}
