package com.difisoft.nhsv.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
 * A CopyMarketLeaderProfitLoss.
 */
@Entity
@Table(name = "t_copy_market_leader_profit_loss")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyMarketLeaderProfitLoss implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "report_date", nullable = false)
    private ZonedDateTime reportDate;

    @Column(name = "net_assets_value", nullable = false)
    private Double netAssetsValue;

    @NotNull
    @Column(name = "profit_loss_ratio", nullable = false)
    private Double profitLossRatio;

    @NotNull
    private String type;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    @ManyToOne
    private User mlUserId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public CopyMarketLeaderProfitLoss id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CopyMarketLeaderProfitLoss reportDate(ZonedDateTime reportDate) {
        this.setReportDate(reportDate);
        return this;
    }

    public void setReportDate(ZonedDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public CopyMarketLeaderProfitLoss netAssetsValue(Double netAssetsValue) {
        this.setNetAssetsValue(netAssetsValue);
        return this;
    }

    public void setNetAssetsValue(Double netAssetsValue) {
        this.netAssetsValue = netAssetsValue;
    }

    public CopyMarketLeaderProfitLoss profitLossRatio(Double profitLossRatio) {
        this.setProfitLossRatio(profitLossRatio);
        return this;
    }

    public void setProfitLossRatio(Double profitLossRatio) {
        this.profitLossRatio = profitLossRatio;
    }

    public CopyMarketLeaderProfitLoss createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CopyMarketLeaderProfitLoss updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setMlUserId(User user) {
        this.mlUserId = user;
    }

    public CopyMarketLeaderProfitLoss mlUserId(User user) {
        this.setMlUserId(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyMarketLeaderProfitLoss)) {
            return false;
        }
        return id != null && id.equals(((CopyMarketLeaderProfitLoss) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyMarketLeaderProfitLoss{" +
            "id=" + getId() +
            ", reportDate='" + getReportDate() + "'" +
            ", netAssetsValue=" + getNetAssetsValue() +
            ", profitLossRatio=" + getProfitLossRatio() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
