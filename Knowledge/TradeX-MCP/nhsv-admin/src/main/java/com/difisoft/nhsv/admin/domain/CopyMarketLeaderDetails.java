package com.difisoft.nhsv.admin.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CopyMarketLeaderDetails.
 */
@Entity
@Table(name = "t_copy_market_leader_details")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyMarketLeaderDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @NotNull
    @Size(max = 255)
    @Column(name = "type", length = 255, nullable = false)
    private String type;

    @NotNull
    @Size(max = 255)
    @Column(name = "label", length = 255, nullable = false)
    private String label;

    @NotNull
    @Size(max = 255)
    @Column(name = "jhi_key", length = 255, nullable = false)
    private String key;

    @NotNull
    @Size(max = 2000)
    @Column(name = "value", length = 2000, nullable = false)
    private String value;

    @ManyToOne
    private User mlUserId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CopyMarketLeaderDetails id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public CopyMarketLeaderDetails createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public CopyMarketLeaderDetails updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getType() {
        return this.type;
    }

    public CopyMarketLeaderDetails type(String type) {
        this.setType(type);
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return this.label;
    }

    public CopyMarketLeaderDetails label(String label) {
        this.setLabel(label);
        return this;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getKey() {
        return this.key;
    }

    public CopyMarketLeaderDetails key(String key) {
        this.setKey(key);
        return this;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public CopyMarketLeaderDetails value(String value) {
        this.setValue(value);
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public User getMlUserId() {
        return this.mlUserId;
    }

    public void setMlUserId(User user) {
        this.mlUserId = user;
    }

    public CopyMarketLeaderDetails mlUserId(User user) {
        this.setMlUserId(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyMarketLeaderDetails)) {
            return false;
        }
        return id != null && id.equals(((CopyMarketLeaderDetails) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyMarketLeaderDetails{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", type='" + getType() + "'" +
            ", label='" + getLabel() + "'" +
            ", key='" + getKey() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
