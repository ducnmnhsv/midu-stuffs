package com.difisoft.nhsv.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CopyPortfolioDetails.
 */
@Entity
@Table(name = "t_copy_portfolio_details")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @JsonIgnoreProperties(value = { "copyPortfolioDetails", "mlUserId" }, allowSetters = true)
    private CopyPortfolio copyPortfolioId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CopyPortfolioDetails id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public CopyPortfolioDetails symbol(String symbol) {
        this.setSymbol(symbol);
        return this;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getWeight() {
        return this.weight;
    }

    public CopyPortfolioDetails weight(Double weight) {
        this.setWeight(weight);
        return this;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public CopyPortfolioDetails createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CopyPortfolio getCopyPortfolioId() {
        return this.copyPortfolioId;
    }

    public void setCopyPortfolioId(CopyPortfolio copyPortfolio) {
        this.copyPortfolioId = copyPortfolio;
    }

    public CopyPortfolioDetails copyPortfolioId(CopyPortfolio copyPortfolio) {
        this.setCopyPortfolioId(copyPortfolio);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyPortfolioDetails)) {
            return false;
        }
        return id != null && id.equals(((CopyPortfolioDetails) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioDetails{" +
            "id=" + getId() +
            ", symbol='" + getSymbol() + "'" +
            ", weight=" + getWeight() +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
