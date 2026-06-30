package com.difisoft.nhsv.admin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLossDetails} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyMarketLeaderProfitLossDetailsDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime reportDate;

    @NotNull
    private String stockCode;

    @NotNull
    private Long stockQuantity;

    @NotNull
    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    private UserDTO mlUserId;

    private CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossId;

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

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public Long getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Long stockQuantity) {
        this.stockQuantity = stockQuantity;
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

    public CopyMarketLeaderProfitLossDTO getCopyMarketLeaderProfitLossId() {
        return copyMarketLeaderProfitLossId;
    }

    public void setCopyMarketLeaderProfitLossId(CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossId) {
        this.copyMarketLeaderProfitLossId = copyMarketLeaderProfitLossId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyMarketLeaderProfitLossDetailsDTO)) {
            return false;
        }

        CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO = (CopyMarketLeaderProfitLossDetailsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copyMarketLeaderProfitLossDetailsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyMarketLeaderProfitLossDetailsDTO{" +
            "id=" + getId() +
            ", reportDate='" + getReportDate() + "'" +
            ", stockCode='" + getStockCode() + "'" +
            ", stockQuantity=" + getStockQuantity() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", mlUserId=" + getMlUserId() +
            ", copyMarketLeaderProfitLossId=" + getCopyMarketLeaderProfitLossId() +
            "}";
    }
}
