package com.difisoft.nhsv.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CopyPortfolioDetailHistory.
 */
@Entity
@Table(name = "t_copy_portfolio_detail_history")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioDetailHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "symbol", length = 255, nullable = false)
    private String symbol;

    @NotNull
    @Column(name = "weight", nullable = false)
    private Double weight;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @ManyToOne
    @JsonIgnoreProperties(value = { "copyPortfolioDetailHistories", "mlUserId" }, allowSetters = true)
    private CopyPortfolioHistory copyPortfolioHistoryId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CopyPortfolioDetailHistory id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public CopyPortfolioDetailHistory symbol(String symbol) {
        this.setSymbol(symbol);
        return this;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getWeight() {
        return this.weight;
    }

    public CopyPortfolioDetailHistory weight(Double weight) {
        this.setWeight(weight);
        return this;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public CopyPortfolioDetailHistory createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CopyPortfolioHistory getCopyPortfolioHistoryId() {
        return this.copyPortfolioHistoryId;
    }

    public void setCopyPortfolioHistoryId(CopyPortfolioHistory copyPortfolioHistory) {
        this.copyPortfolioHistoryId = copyPortfolioHistory;
    }

    public CopyPortfolioDetailHistory copyPortfolioHistoryId(CopyPortfolioHistory copyPortfolioHistory) {
        this.setCopyPortfolioHistoryId(copyPortfolioHistory);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyPortfolioDetailHistory)) {
            return false;
        }
        return id != null && id.equals(((CopyPortfolioDetailHistory) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioDetailHistory{" +
            "id=" + getId() +
            ", symbol='" + getSymbol() + "'" +
            ", weight=" + getWeight() +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
