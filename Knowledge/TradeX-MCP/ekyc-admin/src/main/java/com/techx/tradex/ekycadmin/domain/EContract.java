package com.techx.tradex.ekycadmin.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A EContract.
 */
@Entity
@Table(name = "econtract")
public class EContract implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 36)
    @Column(name = "ref_id", length = 36, nullable = false, unique = true)
    private String refId;

    @NotNull
    @Size(max = 255)
    @Column(name = "envelope_id", length = 255, nullable = false, unique = true)
    private String envelopeId;

    @NotNull
    @Size(max = 255)
    @Column(name = "identifier_id", length = 255, nullable = false)
    private String identifierId;

    @NotNull
    @Size(max = 255)
    @Column(name = "template_id", length = 255, nullable = false)
    private String templateId;

    @Size(max = 255)
    @Column(name = "alias", length = 255)
    private String alias;

    @NotNull
    @Size(max = 255)
    @Column(name = "company_type", length = 255, nullable = false)
    private String companyType;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToOne
    @JoinColumn(unique = true)
    private EKyc eKyc;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EContract id(Long id) {
        this.id = id;
        return this;
    }

    public String getRefId() {
        return this.refId;
    }

    public EContract refId(String refId) {
        this.refId = refId;
        return this;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getEnvelopeId() {
        return this.envelopeId;
    }

    public EContract envelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
        return this;
    }

    public void setEnvelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
    }

    public String getIdentifierId() {
        return this.identifierId;
    }

    public EContract identifierId(String identifierId) {
        this.identifierId = identifierId;
        return this;
    }

    public void setIdentifierId(String identifierId) {
        this.identifierId = identifierId;
    }

    public String getTemplateId() {
        return this.templateId;
    }

    public EContract templateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getAlias() {
        return this.alias;
    }

    public EContract alias(String alias) {
        this.alias = alias;
        return this;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCompanyType() {
        return this.companyType;
    }

    public EContract companyType(String companyType) {
        this.companyType = companyType;
        return this;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public EContract createdAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public EContract updatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public EKyc getEKyc() {
        return this.eKyc;
    }

    public EContract eKyc(EKyc eKyc) {
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
        if (!(o instanceof EContract)) {
            return false;
        }
        return id != null && id.equals(((EContract) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EContract{" +
            "id=" + getId() +
            ", refId='" + getRefId() + "'" +
            ", envelopeId='" + getEnvelopeId() + "'" +
            ", identifierId='" + getIdentifierId() + "'" +
            ", templateId='" + getTemplateId() + "'" +
            ", alias='" + getAlias() + "'" +
            ", companyType='" + getCompanyType() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
