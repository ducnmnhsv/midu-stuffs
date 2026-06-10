package com.techx.tradex.ekycadmin.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;

/**
 * A EKycCreatorStatus.
 */
@Entity
@Table(name = "ekyc_creator_status")
public class EKycCreatorStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "status")
    private String status;

    @Column(name = "reason")
    private String reason;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Lob
    @Column(name = "full_result")
    private String fullResult;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private EKyc eKyc;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EKycCreatorStatus id(Long id) {
        this.id = id;
        return this;
    }

    public String getStatus() {
        return this.status;
    }

    public EKycCreatorStatus status(String status) {
        this.status = status;
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return this.reason;
    }

    public EKycCreatorStatus reason(String reason) {
        this.reason = reason;
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public EKycCreatorStatus updatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public EKycCreatorStatus updatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getFullResult() {
        return this.fullResult;
    }

    public EKycCreatorStatus fullResult(String fullResult) {
        this.fullResult = fullResult;
        return this;
    }

    public void setFullResult(String fullResult) {
        this.fullResult = fullResult;
    }

    public EKyc getEKyc() {
        return this.eKyc;
    }

    public EKycCreatorStatus eKyc(EKyc eKyc) {
        this.setEKyc(eKyc);
        return this;
    }

    public void setEKyc(EKyc eKyc) {
        this.eKyc = eKyc;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EKycCreatorStatus)) {
            return false;
        }
        return id != null && id.equals(((EKycCreatorStatus) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EKycCreatorStatus{" +
            "id=" + getId() +
            ", status='" + getStatus() + "'" +
            ", reason='" + getReason() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", updatedBy='" + getUpdatedBy() + "'" +
            ", fullResult='" + getFullResult() + "'" +
            "}";
    }
}
