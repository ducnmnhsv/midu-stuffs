package com.difisoft.nhsv.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CopyPortfolio.
 */
@Entity
@Table(name = "t_copy_portfolio")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "copyPortfolioId")
    @JsonIgnoreProperties(value = { "copyPortfolioId" }, allowSetters = true)
    private Set<CopyPortfolioDetails> copyPortfolioDetails = new HashSet<>();

    @ManyToOne
    private User mlUserId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CopyPortfolio id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public CopyPortfolio createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<CopyPortfolioDetails> getCopyPortfolioDetails() {
        return this.copyPortfolioDetails;
    }

    public void setCopyPortfolioDetails(Set<CopyPortfolioDetails> copyPortfolioDetails) {
        if (this.copyPortfolioDetails != null) {
            this.copyPortfolioDetails.forEach(i -> i.setCopyPortfolioId(null));
        }
        if (copyPortfolioDetails != null) {
            copyPortfolioDetails.forEach(i -> i.setCopyPortfolioId(this));
        }
        this.copyPortfolioDetails = copyPortfolioDetails;
    }

    public CopyPortfolio copyPortfolioDetails(Set<CopyPortfolioDetails> copyPortfolioDetails) {
        this.setCopyPortfolioDetails(copyPortfolioDetails);
        return this;
    }

    public CopyPortfolio addCopyPortfolioDetails(CopyPortfolioDetails copyPortfolioDetails) {
        this.copyPortfolioDetails.add(copyPortfolioDetails);
        copyPortfolioDetails.setCopyPortfolioId(this);
        return this;
    }

    public CopyPortfolio removeCopyPortfolioDetails(CopyPortfolioDetails copyPortfolioDetails) {
        this.copyPortfolioDetails.remove(copyPortfolioDetails);
        copyPortfolioDetails.setCopyPortfolioId(null);
        return this;
    }

    public User getMlUserId() {
        return this.mlUserId;
    }

    public void setMlUserId(User user) {
        this.mlUserId = user;
    }

    public CopyPortfolio mlUserId(User user) {
        this.setMlUserId(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyPortfolio)) {
            return false;
        }
        return id != null && id.equals(((CopyPortfolio) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolio{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
