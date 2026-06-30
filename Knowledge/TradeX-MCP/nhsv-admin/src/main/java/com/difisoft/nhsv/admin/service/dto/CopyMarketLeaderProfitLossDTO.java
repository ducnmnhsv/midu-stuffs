package com.difisoft.nhsv.admin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyMarketLeaderProfitLossDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime reportDate;

    @NotNull
    private Double netAssetsValue;

    @NotNull
    private Double profitLossRatio;

    @NotNull
    private Double accumulateProfitLossRatio;

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

    public ZonedDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(ZonedDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public Double getNetAssetsValue() {
        return netAssetsValue;
    }

    public void setNetAssetsValue(Double netAssetsValue) {
        this.netAssetsValue = netAssetsValue;
    }

    public Double getProfitLossRatio() {
        return profitLossRatio;
    }

    public void setProfitLossRatio(Double profitLossRatio) {
        this.profitLossRatio = profitLossRatio;
    }

    public Double getAccumulateProfitLossRatio() {
        return accumulateProfitLossRatio;
    }

    public void setAccumulateProfitLossRatio(Double accumulateProfitLossRatio) {
        this.accumulateProfitLossRatio = accumulateProfitLossRatio;
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
        if (!(o instanceof CopyMarketLeaderProfitLossDTO)) {
            return false;
        }

        CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO = (CopyMarketLeaderProfitLossDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copyMarketLeaderProfitLossDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyMarketLeaderProfitLossDTO{" +
            "id=" + getId() +
            ", reportDate='" + getReportDate() + "'" +
            ", netAssetsValue=" + getNetAssetsValue() +
            ", profitLossRatio=" + getProfitLossRatio() +
            ", accumulateProfitLossRatio=" + getAccumulateProfitLossRatio() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", mlUserId=" + getMlUserId() +
            "}";
    }
}
