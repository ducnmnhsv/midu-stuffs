package com.techx.tradex.ekycadmin.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import tech.jhipster.service.filter.ZonedDateTimeFilter;

/**
 * Criteria class for the {@link com.techx.tradex.ekycadmin.domain.EContract} entity. This class is used
 * in {@link com.techx.tradex.ekycadmin.web.rest.EContractResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /e-contracts?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class EContractCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter refId;

    private StringFilter envelopeId;

    private StringFilter identifierId;

    private StringFilter templateId;

    private StringFilter alias;

    private StringFilter companyType;

    private ZonedDateTimeFilter createdAt;

    private ZonedDateTimeFilter updatedAt;

    private LongFilter eKycId;

    public EContractCriteria() {}

    public EContractCriteria(EContractCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.refId = other.refId == null ? null : other.refId.copy();
        this.envelopeId = other.envelopeId == null ? null : other.envelopeId.copy();
        this.identifierId = other.identifierId == null ? null : other.identifierId.copy();
        this.templateId = other.templateId == null ? null : other.templateId.copy();
        this.alias = other.alias == null ? null : other.alias.copy();
        this.companyType = other.companyType == null ? null : other.companyType.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.eKycId = other.eKycId == null ? null : other.eKycId.copy();
    }

    @Override
    public EContractCriteria copy() {
        return new EContractCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getRefId() {
        return refId;
    }

    public StringFilter refId() {
        if (refId == null) {
            refId = new StringFilter();
        }
        return refId;
    }

    public void setRefId(StringFilter refId) {
        this.refId = refId;
    }

    public StringFilter getEnvelopeId() {
        return envelopeId;
    }

    public StringFilter envelopeId() {
        if (envelopeId == null) {
            envelopeId = new StringFilter();
        }
        return envelopeId;
    }

    public void setEnvelopeId(StringFilter envelopeId) {
        this.envelopeId = envelopeId;
    }

    public StringFilter getIdentifierId() {
        return identifierId;
    }

    public StringFilter identifierId() {
        if (identifierId == null) {
            identifierId = new StringFilter();
        }
        return identifierId;
    }

    public void setIdentifierId(StringFilter identifierId) {
        this.identifierId = identifierId;
    }

    public StringFilter getTemplateId() {
        return templateId;
    }

    public StringFilter templateId() {
        if (templateId == null) {
            templateId = new StringFilter();
        }
        return templateId;
    }

    public void setTemplateId(StringFilter templateId) {
        this.templateId = templateId;
    }

    public StringFilter getAlias() {
        return alias;
    }

    public StringFilter alias() {
        if (alias == null) {
            alias = new StringFilter();
        }
        return alias;
    }

    public void setAlias(StringFilter alias) {
        this.alias = alias;
    }

    public StringFilter getCompanyType() {
        return companyType;
    }

    public StringFilter companyType() {
        if (companyType == null) {
            companyType = new StringFilter();
        }
        return companyType;
    }

    public void setCompanyType(StringFilter companyType) {
        this.companyType = companyType;
    }

    public ZonedDateTimeFilter getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTimeFilter createdAt() {
        if (createdAt == null) {
            createdAt = new ZonedDateTimeFilter();
        }
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTimeFilter createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTimeFilter getUpdatedAt() {
        return updatedAt;
    }

    public ZonedDateTimeFilter updatedAt() {
        if (updatedAt == null) {
            updatedAt = new ZonedDateTimeFilter();
        }
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTimeFilter updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LongFilter getEKycId() {
        return eKycId;
    }

    public LongFilter eKycId() {
        if (eKycId == null) {
            eKycId = new LongFilter();
        }
        return eKycId;
    }

    public void setEKycId(LongFilter eKycId) {
        this.eKycId = eKycId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EContractCriteria that = (EContractCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(refId, that.refId) &&
            Objects.equals(envelopeId, that.envelopeId) &&
            Objects.equals(identifierId, that.identifierId) &&
            Objects.equals(templateId, that.templateId) &&
            Objects.equals(alias, that.alias) &&
            Objects.equals(companyType, that.companyType) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(eKycId, that.eKycId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, refId, envelopeId, identifierId, templateId, alias, companyType, createdAt, updatedAt, eKycId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EContractCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (refId != null ? "refId=" + refId + ", " : "") +
            (envelopeId != null ? "envelopeId=" + envelopeId + ", " : "") +
            (identifierId != null ? "identifierId=" + identifierId + ", " : "") +
            (templateId != null ? "templateId=" + templateId + ", " : "") +
            (alias != null ? "alias=" + alias + ", " : "") +
            (companyType != null ? "companyType=" + companyType + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (eKycId != null ? "eKycId=" + eKycId + ", " : "") +
            "}";
    }
}
