package com.difisoft.nhsv.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CopySubscriberDetails.
 */
@Entity
@Table(name = "t_copy_subscriber_details")
@SuppressWarnings("common-java:DuplicatedBlocks")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CopySubscriberDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "username", nullable = false)
    private String username;

    @NotNull
    @Column(name = "identifier_number", nullable = false)
    private String identifierNumber;

    @NotNull
    @Column(name = "branch_code", nullable = false)
    private String branchCode;

    @NotNull
    @Column(name = "mng_dept_code", nullable = false)
    private String mngDeptCode;

    @NotNull
    @Column(name = "dept_code", nullable = false)
    private String deptCode;

    @NotNull
    @Column(name = "agency_number", nullable = false)
    private String agencyNumber;

    @NotNull
    @Column(name = "account_numbers", nullable = false)
    private String accountNumbers;

    @Column(name = "user_level")
    private String userLevel;

    @ManyToOne
    @JsonIgnoreProperties(value = { "copySubscriberDetails", "mlUserId" }, allowSetters = true)
    private CopySubscriber copySubscriberId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CopySubscriberDetails id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public CopySubscriberDetails username(String username) {
        this.setUsername(username);
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdentifierNumber() {
        return this.identifierNumber;
    }

    public CopySubscriberDetails identifierNumber(String identifierNumber) {
        this.setIdentifierNumber(identifierNumber);
        return this;
    }

    public void setIdentifierNumber(String identifierNumber) {
        this.identifierNumber = identifierNumber;
    }

    public String getBranchCode() {
        return this.branchCode;
    }

    public CopySubscriberDetails branchCode(String branchCode) {
        this.setBranchCode(branchCode);
        return this;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getMngDeptCode() {
        return this.mngDeptCode;
    }

    public CopySubscriberDetails mngDeptCode(String mngDeptCode) {
        this.setMngDeptCode(mngDeptCode);
        return this;
    }

    public void setMngDeptCode(String mngDeptCode) {
        this.mngDeptCode = mngDeptCode;
    }

    public String getDeptCode() {
        return this.deptCode;
    }

    public CopySubscriberDetails deptCode(String deptCode) {
        this.setDeptCode(deptCode);
        return this;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getAgencyNumber() {
        return this.agencyNumber;
    }

    public CopySubscriberDetails agencyNumber(String agencyNumber) {
        this.setAgencyNumber(agencyNumber);
        return this;
    }

    public void setAgencyNumber(String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    public String getAccountNumbers() {
        return this.accountNumbers;
    }

    public CopySubscriberDetails accountNumbers(String accountNumbers) {
        this.setAccountNumbers(accountNumbers);
        return this;
    }

    public void setAccountNumbers(String accountNumbers) {
        this.accountNumbers = accountNumbers;
    }

    public String getUserLevel() {
        return this.userLevel;
    }

    public CopySubscriberDetails userLevel(String userLevel) {
        this.setUserLevel(userLevel);
        return this;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public CopySubscriber getCopySubscriber() {
        return this.copySubscriberId;
    }

    public void setCopySubscriberId(CopySubscriber copySubscriber) {
        this.copySubscriberId = copySubscriber;
    }

    public CopySubscriberDetails copySubscriber(CopySubscriber copySubscriber) {
        this.setCopySubscriberId(copySubscriber);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopySubscriberDetails)) {
            return false;
        }
        return id != null && id.equals(((CopySubscriberDetails) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopySubscriberDetails{" +
            "id=" + getId() +
            ", username='" + getUsername() + "'" +
            ", identifierNumber='" + getIdentifierNumber() + "'" +
            ", branchCode='" + getBranchCode() + "'" +
            ", mngDeptCode='" + getMngDeptCode() + "'" +
            ", deptCode='" + getDeptCode() + "'" +
            ", agencyNumber='" + getAgencyNumber() + "'" +
            ", accountNumbers='" + getAccountNumbers() + "'" +
            ", userLevel='" + getUserLevel() + "'" +
            "}";
    }
}
