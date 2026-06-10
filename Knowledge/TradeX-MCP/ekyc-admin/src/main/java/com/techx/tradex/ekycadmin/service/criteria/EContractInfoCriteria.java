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
 * Criteria class for the {@link com.techx.tradex.ekycadmin.domain.EContractInfo} entity. This class is used
 * in {@link com.techx.tradex.ekycadmin.web.rest.EContractInfoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /e-contract-infos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class EContractInfoCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private ZonedDateTimeFilter createdAt;

    private ZonedDateTimeFilter updatedAt;

    private StringFilter templateId;

    private StringFilter contactId;


    private StringFilter contractStatus;

    private StringFilter signFileContent;

    private StringFilter contractFileContent;

    private LongFilter eContractId;

    public EContractInfoCriteria() {}

    public EContractInfoCriteria(EContractInfoCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.templateId = other.templateId == null ? null : other.templateId.copy();
        this.contactId = other.contactId == null ? null : other.contactId.copy();
        this.contractStatus = other.contractStatus == null ? null : other.contractStatus.copy();
        this.signFileContent = other.signFileContent == null ? null : other.signFileContent.copy();
        this.contractFileContent = other.contractFileContent == null ? null : other.contractFileContent.copy();
        this.eContractId = other.eContractId == null ? null : other.eContractId.copy();
    }

    @Override
    public EContractInfoCriteria copy() {
        return new EContractInfoCriteria(this);
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

    public StringFilter getContactId() {
        return contactId;
    }

    public StringFilter contactId() {
        if (contactId == null) {
            contactId = new StringFilter();
        }
        return contactId;
    }

    public void setContactId(StringFilter contactId) {
        this.contactId = contactId;
    }

    public StringFilter getContractStatus() {
        return contractStatus;
    }

    public StringFilter contractStatus() {
        if (contractStatus == null) {
            contractStatus = new StringFilter();
        }
        return contractStatus;
    }

    public void setContractStatus(StringFilter contractStatus) {
        this.contractStatus = contractStatus;
    }

    public StringFilter getSignFileContent() {
        return signFileContent;
    }

    public StringFilter signFileContent() {
        if (signFileContent == null) {
            signFileContent = new StringFilter();
        }
        return signFileContent;
    }

    public void setSignFileContent(StringFilter signFileContent) {
        this.signFileContent = signFileContent;
    }

    public StringFilter getContractFileContent() {
        return contractFileContent;
    }

    public StringFilter contractFileContent() {
        if (contractFileContent == null) {
            contractFileContent = new StringFilter();
        }
        return contractFileContent;
    }

    public void setContractFileContent(StringFilter contractFileContent) {
        this.contractFileContent = contractFileContent;
    }

    public LongFilter getEContractId() {
        return eContractId;
    }

    public LongFilter eContractId() {
        if (eContractId == null) {
            eContractId = new LongFilter();
        }
        return eContractId;
    }

    public void setEContractId(LongFilter eContractId) {
        this.eContractId = eContractId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EContractInfoCriteria that = (EContractInfoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(templateId, that.templateId) &&
            Objects.equals(contactId, that.contactId) &&
            Objects.equals(contractStatus, that.contractStatus) &&
            Objects.equals(signFileContent, that.signFileContent) &&
            Objects.equals(contractFileContent, that.contractFileContent) &&
            Objects.equals(eContractId, that.eContractId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            createdAt,
            updatedAt,
            templateId,
            contactId,
            contractStatus,
            signFileContent,
            contractFileContent,
            eContractId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EContractInfoCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (templateId != null ? "templateId=" + templateId + ", " : "") +
            (contactId != null ? "contactId=" + contactId + ", " : "") +
            (contractStatus != null ? "contractStatus=" + contractStatus + ", " : "") +
            (signFileContent != null ? "signFileContent=" + signFileContent + ", " : "") +
            (contractFileContent != null ? "contractFileContent=" + contractFileContent + ", " : "") +
            (eContractId != null ? "eContractId=" + eContractId + ", " : "") +
            "}";
    }
}
