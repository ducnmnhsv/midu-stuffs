package com.techx.tradex.ekycadmin.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;

/**
 * A MatchingRate.
 */
@Entity
@Table(name = "matching_rate")
public class MatchingRate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "core")
    private String core;

    @Column(name = "matching_rate")
    private Double matchingRate;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MatchingRate id(Long id) {
        this.id = id;
        return this;
    }

    public String getCore() {
        return this.core;
    }

    public MatchingRate core(String core) {
        this.core = core;
        return this;
    }

    public void setCore(String core) {
        this.core = core;
    }

    public Double getMatchingRate() {
        return this.matchingRate;
    }

    public MatchingRate matchingRate(Double matchingRate) {
        this.matchingRate = matchingRate;
        return this;
    }

    public void setMatchingRate(Double matchingRate) {
        this.matchingRate = matchingRate;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public MatchingRate createdAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public MatchingRate updatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MatchingRate)) {
            return false;
        }
        return id != null && id.equals(((MatchingRate) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MatchingRate{" +
            "id=" + getId() +
            ", core='" + getCore() + "'" +
            ", matchingRate=" + getMatchingRate() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
