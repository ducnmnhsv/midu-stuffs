package com.techx.tradex.ekycadmin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.persistence.Lob;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.techx.tradex.ekycadmin.domain.EContractInfo} entity.
 */
public class EContractInfoDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    @NotNull
    @Size(max = 255)
    private String templateId;

    @Lob
    private String requestData;

    private String contactId;

    private String contactIdAction;

    private String contractStatus;

    private String signFileContent;

    private String contractFileContent;

    private EContractDTO eContract;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactIdAction() {
        return contactIdAction;
    }

    public void setContactIdAction(String contactIdAction) {
        this.contactIdAction = contactIdAction;
    }

    public String getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
    }

    public String getSignFileContent() {
        return signFileContent;
    }

    public void setSignFileContent(String signFileContent) {
        this.signFileContent = signFileContent;
    }

    public String getContractFileContent() {
        return contractFileContent;
    }

    public void setContractFileContent(String contractFileContent) {
        this.contractFileContent = contractFileContent;
    }

    public EContractDTO geteContract() {
        return eContract;
    }

    public void seteContract(EContractDTO eContract) {
        this.eContract = eContract;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EContractInfoDTO)) {
            return false;
        }

        EContractInfoDTO eContractInfoDTO = (EContractInfoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, eContractInfoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EContractInfoDTO{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", templateId='" + getTemplateId() + "'" +
            ", requestData='" + getRequestData() + "'" +
            ", contactId='" + getContactId() + "'" +
            ", contactIdAction='" + getContactIdAction() + "'" +
            ", contractStatus='" + getContractStatus() + "'" +
            ", signFileContent='" + getSignFileContent() + "'" +
            ", contractFileContent='" + getContractFileContent() + "'" +
            ", eContract=" + geteContract() +
            "}";
    }
}
