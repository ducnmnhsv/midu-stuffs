package com.difisoft.nhsv.admin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetails} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioDetailsDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String symbol;

    @NotNull
    private Double weight;

    @NotNull
    private ZonedDateTime createdAt;

    private CopyPortfolioDTO copyPortfolioId;

    private CopyPortfolioDTO copyPortfolio;

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

    public CopyPortfolioDTO getCopyPortfolioId() {
        return copyPortfolioId;
    }

    public void setCopyPortfolioId(CopyPortfolioDTO copyPortfolioId) {
        this.copyPortfolioId = copyPortfolioId;
    }

    public CopyPortfolioDTO getCopyPortfolio() {
        return copyPortfolio;
    }

    public void setCopyPortfolio(CopyPortfolioDTO copyPortfolio) {
        this.copyPortfolio = copyPortfolio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyPortfolioDetailsDTO)) {
            return false;
        }

        CopyPortfolioDetailsDTO copyPortfolioDetailsDTO = (CopyPortfolioDetailsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copyPortfolioDetailsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioDetailsDTO{" +
            "id=" + getId() +
            ", symbol='" + getSymbol() + "'" +
            ", weight=" + getWeight() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", copyPortfolioId=" + getCopyPortfolioId() +
            ", copyPortfolio=" + getCopyPortfolio() +
            "}";
    }
}
