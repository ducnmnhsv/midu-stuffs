package com.techx.tradex.ekycadmin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.techx.tradex.ekycadmin.domain.EContract} entity.
 */
public class EContractDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 36)
    private String refId;

    @NotNull
    @Size(max = 255)
    private String envelopeId;

    @NotNull
    @Size(max = 255)
    private String identifierId;

    @NotNull
    @Size(max = 255)
    private String templateId;

    @Size(max = 255)
    private String alias;

    @NotNull
    @Size(max = 255)
    private String companyType;

    @NotNull
    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    // private EKycDTO eKyc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
    }

    public String getIdentifierId() {
        return identifierId;
    }

    public void setIdentifierId(String identifierId) {
        this.identifierId = identifierId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // public EKycDTO geteKyc() {
    //     return eKyc;
    // }

    // public void seteKyc(EKycDTO eKyc) {
    //     this.eKyc = eKyc;
    // }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EContractDTO)) {
            return false;
        }

        EContractDTO eContractDTO = (EContractDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, eContractDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EContractDTO{" +
            "id=" + getId() +
            ", refId='" + getRefId() + "'" +
            ", envelopeId='" + getEnvelopeId() + "'" +
            ", identifierId='" + getIdentifierId() + "'" +
            ", templateId='" + getTemplateId() + "'" +
            ", alias='" + getAlias() + "'" +
            ", companyType='" + getCompanyType() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
//            ", eKyc=" + geteKyc() +
            "}";
    }
}
