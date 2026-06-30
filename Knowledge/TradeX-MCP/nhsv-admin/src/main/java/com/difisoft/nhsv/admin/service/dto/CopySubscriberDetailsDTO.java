package com.difisoft.nhsv.admin.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopySubscriberDetails} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopySubscriberDetailsDTO implements Serializable {

    private Long id;

    @NotNull
    private String username;

    @NotNull
    private String identifierNumber;

    @NotNull
    private String branchCode;

    @NotNull
    private String mngDeptCode;

    @NotNull
    private String deptCode;

    @NotNull
    private String agencyNumber;

    @NotNull
    private String accountNumbers;

    private String userLevel;

    private CopySubscriberDTO copySubscriber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdentifierNumber() {
        return identifierNumber;
    }

    public void setIdentifierNumber(String identifierNumber) {
        this.identifierNumber = identifierNumber;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getMngDeptCode() {
        return mngDeptCode;
    }

    public void setMngDeptCode(String mngDeptCode) {
        this.mngDeptCode = mngDeptCode;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getAgencyNumber() {
        return agencyNumber;
    }

    public void setAgencyNumber(String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    public String getAccountNumbers() {
        return accountNumbers;
    }

    public void setAccountNumbers(String accountNumbers) {
        this.accountNumbers = accountNumbers;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public CopySubscriberDTO getCopySubscriber() {
        return copySubscriber;
    }

    public void setCopySubscriber(CopySubscriberDTO copySubscriber) {
        this.copySubscriber = copySubscriber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopySubscriberDetailsDTO)) {
            return false;
        }

        CopySubscriberDetailsDTO copySubscriberDetailsDTO = (CopySubscriberDetailsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copySubscriberDetailsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopySubscriberDetailsDTO{" +
            "id=" + getId() +
            ", username='" + getUsername() + "'" +
            ", identifierNumber='" + getIdentifierNumber() + "'" +
            ", branchCode='" + getBranchCode() + "'" +
            ", mngDeptCode='" + getMngDeptCode() + "'" +
            ", deptCode='" + getDeptCode() + "'" +
            ", agencyNumber='" + getAgencyNumber() + "'" +
            ", accountNumbers='" + getAccountNumbers() + "'" +
            ", userLevel='" + getUserLevel() + "'" +
            ", copySubscriber=" + getCopySubscriber() +
            "}";
    }
}
