package com.difisoft.nhsv.admin.domain;

import com.difisoft.nhsv.admin.domain.enumeration.OrderSetTypeEnum;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CopySubscriberHistory.
 */
@Entity
@Table(name = "t_copy_subscriber_history")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopySubscriberHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "account_number", length = 255, nullable = false)
    private String accountNumber;

    @NotNull
    @Size(max = 255)
    @Column(name = "sub_number", length = 255, nullable = false)
    private String subNumber;

    @NotNull
    @Size(max = 255)
    @Column(name = "user_name", length = 255, nullable = false)
    private String userName;

    @NotNull
    @Column(name = "allocated_ratio", nullable = false)
    private Double allocatedRatio;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_set_type")
    private OrderSetTypeEnum orderSetType;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @ManyToOne
    private User mlUserId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CopySubscriberHistory id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public CopySubscriberHistory accountNumber(String accountNumber) {
        this.setAccountNumber(accountNumber);
        return this;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSubNumber() {
        return this.subNumber;
    }

    public CopySubscriberHistory subNumber(String subNumber) {
        this.setSubNumber(subNumber);
        return this;
    }

    public void setSubNumber(String subNumber) {
        this.subNumber = subNumber;
    }

    public String getUserName() {
        return this.userName;
    }

    public CopySubscriberHistory userName(String userName) {
        this.setUserName(userName);
        return this;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Double getAllocatedRatio() {
        return this.allocatedRatio;
    }

    public CopySubscriberHistory allocatedRatio(Double allocatedRatio) {
        this.setAllocatedRatio(allocatedRatio);
        return this;
    }

    public void setAllocatedRatio(Double allocatedRatio) {
        this.allocatedRatio = allocatedRatio;
    }

    public OrderSetTypeEnum getOrderSetType() {
        return this.orderSetType;
    }

    public CopySubscriberHistory orderSetType(OrderSetTypeEnum orderSetType) {
        this.setOrderSetType(orderSetType);
        return this;
    }

    public void setOrderSetType(OrderSetTypeEnum orderSetType) {
        this.orderSetType = orderSetType;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public CopySubscriberHistory createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public CopySubscriberHistory updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getMlUserId() {
        return this.mlUserId;
    }

    public void setMlUserId(User user) {
        this.mlUserId = user;
    }

    public CopySubscriberHistory mlUserId(User user) {
        this.setMlUserId(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopySubscriberHistory)) {
            return false;
        }
        return id != null && id.equals(((CopySubscriberHistory) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopySubscriberHistory{" +
            "id=" + getId() +
            ", accountNumber='" + getAccountNumber() + "'" +
            ", subNumber='" + getSubNumber() + "'" +
            ", userName='" + getUserName() + "'" +
            ", allocatedRatio=" + getAllocatedRatio() +
            ", orderSetType='" + getOrderSetType() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
