package com.difisoft.nhsv.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * A CopyMarketLeaderProfitLossDetails.
 */
@Entity
@Table(name = "t_copy_market_leader_profit_loss_details")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyMarketLeaderProfitLossDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "report_date", nullable = false)
    private ZonedDateTime reportDate;

    @NotNull
    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @NotNull
    @Column(name = "stock_quantity", nullable = false)
    private Long stockQuantity;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    @ManyToOne
    private User mlUserId;

    @ManyToOne
    @JsonIgnoreProperties(value = {"mlUserId"}, allowSetters = true)
    private CopyMarketLeaderProfitLoss copyMarketLeaderProfitLossId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CopyMarketLeaderProfitLossDetails id(Long id) {
        this.setId(id);
        return this;
    }

    public ZonedDateTime getReportDate() {
        return this.reportDate;
    }

    public void setReportDate(ZonedDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public CopyMarketLeaderProfitLossDetails reportDate(ZonedDateTime reportDate) {
        this.setReportDate(reportDate);
        return this;
    }

    public String getStockCode() {
        return this.stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public CopyMarketLeaderProfitLossDetails stockCode(String stockCode) {
        this.setStockCode(stockCode);
        return this;
    }

    public Long getStockQuantity() {
        return this.stockQuantity;
    }

    public void setStockQuantity(Long stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public CopyMarketLeaderProfitLossDetails stockQuantity(Long stockQuantity) {
        this.setStockQuantity(stockQuantity);
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CopyMarketLeaderProfitLossDetails createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public CopyMarketLeaderProfitLossDetails updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public User getMlUserId() {
        return this.mlUserId;
    }

    public void setMlUserId(User user) {
        this.mlUserId = user;
    }

    public CopyMarketLeaderProfitLossDetails mlUserId(User user) {
        this.setMlUserId(user);
        return this;
    }

    public CopyMarketLeaderProfitLoss getCopyMarketLeaderProfitLossId() {
        return this.copyMarketLeaderProfitLossId;
    }

    public void setCopyMarketLeaderProfitLossId(CopyMarketLeaderProfitLoss copyMarketLeaderProfitLoss) {
        this.copyMarketLeaderProfitLossId = copyMarketLeaderProfitLoss;
    }

    public CopyMarketLeaderProfitLossDetails copyMarketLeaderProfitLossId(CopyMarketLeaderProfitLoss copyMarketLeaderProfitLoss) {
        this.setCopyMarketLeaderProfitLossId(copyMarketLeaderProfitLoss);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyMarketLeaderProfitLossDetails)) {
            return false;
        }
        return id != null && id.equals(((CopyMarketLeaderProfitLossDetails) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyMarketLeaderProfitLossDetails{" +
            "id=" + getId() +
            ", reportDate='" + getReportDate() + "'" +
            ", stockCode='" + getStockCode() + "'" +
            ", stockQuantity=" + getStockQuantity() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
