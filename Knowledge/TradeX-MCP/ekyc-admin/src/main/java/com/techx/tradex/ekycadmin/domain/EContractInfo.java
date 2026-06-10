package com.techx.tradex.ekycadmin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A EContractInfo.
 */
@Entity
@Table(name = "econtract_info")
@Data
public class EContractInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @NotNull
    @Size(max = 255)
    @Column(name = "template_id", length = 255, nullable = false)
    private String templateId;

    @Lob
    @Column(name = "request_data", nullable = false)
    private String requestData;

    @Column(name = "contact_id")
    private String contactId;

    @Column(name = "contract_status")
    private String contractStatus;

    @Column(name = "sign_file_content")
    private String signFileContent;

    @Column(name = "contract_file_content")
    private String contractFileContent;

    @Column(name = "customer_signatue_status")
    private String customerSignatueStatus;

    @Column(name = "securities_signature_status")
    private String securitiesSignatureStatus;

    @JsonIgnoreProperties(value = { "eKyc" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private EContract eContract;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EContractInfo id(Long id) {
        this.id = id;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public EContractInfo createdAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public EContractInfo updatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTemplateId() {
        return this.templateId;
    }

    public EContractInfo templateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getRequestData() {
        return this.requestData;
    }

    public EContractInfo requestData(String requestData) {
        this.requestData = requestData;
        return this;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getContactId() {
        return this.contactId;
    }

    public EContractInfo contactId(String contactId) {
        this.contactId = contactId;
        return this;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContractStatus() {
        return this.contractStatus;
    }

    public EContractInfo contractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
        return this;
    }

    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
    }

    public String getSignFileContent() {
        return this.signFileContent;
    }

    public EContractInfo signFileContent(String signFileContent) {
        this.signFileContent = signFileContent;
        return this;
    }

    public void setSignFileContent(String signFileContent) {
        this.signFileContent = signFileContent;
    }

    public String getContractFileContent() {
        return this.contractFileContent;
    }

    public EContractInfo contractFileContent(String contractFileContent) {
        this.contractFileContent = contractFileContent;
        return this;
    }

    public void setContractFileContent(String contractFileContent) {
        this.contractFileContent = contractFileContent;
    }

    public EContract getEContract() {
        return this.eContract;
    }

    public EContractInfo eContract(EContract eContract) {
        this.setEContract(eContract);
        return this;
    }

    public void setEContract(EContract eContract) {
        this.eContract = eContract;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EContractInfo)) {
            return false;
        }
        return id != null && id.equals(((EContractInfo) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EContractInfo{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", templateId='" + getTemplateId() + "'" +
            ", requestData='" + getRequestData() + "'" +
            ", contactId='" + getContactId() + "'" +
            ", contractStatus='" + getContractStatus() + "'" +
            ", signFileContent='" + getSignFileContent() + "'" +
            ", contractFileContent='" + getContractFileContent() + "'" +
            "}";
    }
}
