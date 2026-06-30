package com.difisoft.nhsv.admin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioDetailHistoryDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String symbol;

    @NotNull
    private Double weight;

    @NotNull
    private ZonedDateTime createdAt;

    private CopyPortfolioHistoryDTO copyPortfolioHistoryId;

    private CopyPortfolioHistoryDTO copyPortfolioHistory;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CopyPortfolioHistoryDTO getCopyPortfolioHistoryId() {
        return copyPortfolioHistoryId;
    }

    public void setCopyPortfolioHistoryId(CopyPortfolioHistoryDTO copyPortfolioHistoryId) {
        this.copyPortfolioHistoryId = copyPortfolioHistoryId;
    }

    public CopyPortfolioHistoryDTO getCopyPortfolioHistory() {
        return copyPortfolioHistory;
    }

    public void setCopyPortfolioHistory(CopyPortfolioHistoryDTO copyPortfolioHistory) {
        this.copyPortfolioHistory = copyPortfolioHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyPortfolioDetailHistoryDTO)) {
            return false;
        }

        CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO = (CopyPortfolioDetailHistoryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copyPortfolioDetailHistoryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioDetailHistoryDTO{" +
            "id=" + getId() +
            ", symbol='" + getSymbol() + "'" +
            ", weight=" + getWeight() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", copyPortfolioHistoryId=" + getCopyPortfolioHistoryId() +
            ", copyPortfolioHistory=" + getCopyPortfolioHistory() +
            "}";
    }
}
