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
 * A CopyPortfolioHistory.
 */
@Entity
@Table(name = "t_copy_portfolio_history")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "copyPortfolioHistoryId")
    @JsonIgnoreProperties(value = { "copyPortfolioHistoryId" }, allowSetters = true)
    private Set<CopyPortfolioDetailHistory> copyPortfolioDetailHistories = new HashSet<>();

    @ManyToOne
    private User mlUserId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CopyPortfolioHistory id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public CopyPortfolioHistory createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<CopyPortfolioDetailHistory> getCopyPortfolioDetailHistories() {
        return this.copyPortfolioDetailHistories;
    }

    public void setCopyPortfolioDetailHistories(Set<CopyPortfolioDetailHistory> copyPortfolioDetailHistories) {
        if (this.copyPortfolioDetailHistories != null) {
            this.copyPortfolioDetailHistories.forEach(i -> i.setCopyPortfolioHistoryId(null));
        }
        if (copyPortfolioDetailHistories != null) {
            copyPortfolioDetailHistories.forEach(i -> i.setCopyPortfolioHistoryId(this));
        }
        this.copyPortfolioDetailHistories = copyPortfolioDetailHistories;
    }

    public CopyPortfolioHistory copyPortfolioDetailHistories(Set<CopyPortfolioDetailHistory> copyPortfolioDetailHistories) {
        this.setCopyPortfolioDetailHistories(copyPortfolioDetailHistories);
        return this;
    }

    public CopyPortfolioHistory addCopyPortfolioDetailHistory(CopyPortfolioDetailHistory copyPortfolioDetailHistory) {
        this.copyPortfolioDetailHistories.add(copyPortfolioDetailHistory);
        copyPortfolioDetailHistory.setCopyPortfolioHistoryId(this);
        return this;
    }

    public CopyPortfolioHistory removeCopyPortfolioDetailHistory(CopyPortfolioDetailHistory copyPortfolioDetailHistory) {
        this.copyPortfolioDetailHistories.remove(copyPortfolioDetailHistory);
        copyPortfolioDetailHistory.setCopyPortfolioHistoryId(null);
        return this;
    }

    public User getMlUserId() {
        return this.mlUserId;
    }

    public void setMlUserId(User user) {
        this.mlUserId = user;
    }

    public CopyPortfolioHistory mlUserId(User user) {
        this.setMlUserId(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyPortfolioHistory)) {
            return false;
        }
        return id != null && id.equals(((CopyPortfolioHistory) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioHistory{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
