package com.difisoft.nhsv.admin.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.CopySubscriberDetails} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.CopySubscriberDetailsResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /copy-subscriber-details?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopySubscriberDetailsCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter username;

    private StringFilter identifierNumber;

    private StringFilter branchCode;

    private StringFilter mngDeptCode;

    private StringFilter deptCode;

    private StringFilter agencyNumber;

    private StringFilter accountNumbers;

    private StringFilter userLevel;

    private LongFilter copySubscriberId;

    private Boolean distinct;

    public CopySubscriberDetailsCriteria() {}

    public CopySubscriberDetailsCriteria(CopySubscriberDetailsCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.username = other.username == null ? null : other.username.copy();
        this.identifierNumber = other.identifierNumber == null ? null : other.identifierNumber.copy();
        this.branchCode = other.branchCode == null ? null : other.branchCode.copy();
        this.mngDeptCode = other.mngDeptCode == null ? null : other.mngDeptCode.copy();
        this.deptCode = other.deptCode == null ? null : other.deptCode.copy();
        this.agencyNumber = other.agencyNumber == null ? null : other.agencyNumber.copy();
        this.accountNumbers = other.accountNumbers == null ? null : other.accountNumbers.copy();
        this.userLevel = other.userLevel == null ? null : other.userLevel.copy();
        this.copySubscriberId = other.copySubscriberId == null ? null : other.copySubscriberId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CopySubscriberDetailsCriteria copy() {
        return new CopySubscriberDetailsCriteria(this);
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

    public StringFilter getUsername() {
        return username;
    }

    public StringFilter username() {
        if (username == null) {
            username = new StringFilter();
        }
        return username;
    }

    public void setUsername(StringFilter username) {
        this.username = username;
    }

    public StringFilter getIdentifierNumber() {
        return identifierNumber;
    }

    public StringFilter identifierNumber() {
        if (identifierNumber == null) {
            identifierNumber = new StringFilter();
        }
        return identifierNumber;
    }

    public void setIdentifierNumber(StringFilter identifierNumber) {
        this.identifierNumber = identifierNumber;
    }

    public StringFilter getBranchCode() {
        return branchCode;
    }

    public StringFilter branchCode() {
        if (branchCode == null) {
            branchCode = new StringFilter();
        }
        return branchCode;
    }

    public void setBranchCode(StringFilter branchCode) {
        this.branchCode = branchCode;
    }

    public StringFilter getMngDeptCode() {
        return mngDeptCode;
    }

    public StringFilter mngDeptCode() {
        if (mngDeptCode == null) {
            mngDeptCode = new StringFilter();
        }
        return mngDeptCode;
    }

    public void setMngDeptCode(StringFilter mngDeptCode) {
        this.mngDeptCode = mngDeptCode;
    }

    public StringFilter getDeptCode() {
        return deptCode;
    }

    public StringFilter deptCode() {
        if (deptCode == null) {
            deptCode = new StringFilter();
        }
        return deptCode;
    }

    public void setDeptCode(StringFilter deptCode) {
        this.deptCode = deptCode;
    }

    public StringFilter getAgencyNumber() {
        return agencyNumber;
    }

    public StringFilter agencyNumber() {
        if (agencyNumber == null) {
            agencyNumber = new StringFilter();
        }
        return agencyNumber;
    }

    public void setAgencyNumber(StringFilter agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    public StringFilter getAccountNumbers() {
        return accountNumbers;
    }

    public StringFilter accountNumbers() {
        if (accountNumbers == null) {
            accountNumbers = new StringFilter();
        }
        return accountNumbers;
    }

    public void setAccountNumbers(StringFilter accountNumbers) {
        this.accountNumbers = accountNumbers;
    }

    public StringFilter getUserLevel() {
        return userLevel;
    }

    public StringFilter userLevel() {
        if (userLevel == null) {
            userLevel = new StringFilter();
        }
        return userLevel;
    }

    public void setUserLevel(StringFilter userLevel) {
        this.userLevel = userLevel;
    }

    public LongFilter getCopySubscriberId() {
        return copySubscriberId;
    }

    public LongFilter copySubscriberId() {
        if (copySubscriberId == null) {
            copySubscriberId = new LongFilter();
        }
        return copySubscriberId;
    }

    public void setCopySubscriberId(LongFilter copySubscriberId) {
        this.copySubscriberId = copySubscriberId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CopySubscriberDetailsCriteria that = (CopySubscriberDetailsCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(username, that.username) &&
            Objects.equals(identifierNumber, that.identifierNumber) &&
            Objects.equals(branchCode, that.branchCode) &&
            Objects.equals(mngDeptCode, that.mngDeptCode) &&
            Objects.equals(deptCode, that.deptCode) &&
            Objects.equals(agencyNumber, that.agencyNumber) &&
            Objects.equals(accountNumbers, that.accountNumbers) &&
            Objects.equals(userLevel, that.userLevel) &&
            Objects.equals(copySubscriberId, that.copySubscriberId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            username,
            identifierNumber,
            branchCode,
            mngDeptCode,
            deptCode,
            agencyNumber,
            accountNumbers,
            userLevel,
            copySubscriberId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopySubscriberDetailsCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (username != null ? "username=" + username + ", " : "") +
            (identifierNumber != null ? "identifierNumber=" + identifierNumber + ", " : "") +
            (branchCode != null ? "branchCode=" + branchCode + ", " : "") +
            (mngDeptCode != null ? "mngDeptCode=" + mngDeptCode + ", " : "") +
            (deptCode != null ? "deptCode=" + deptCode + ", " : "") +
            (agencyNumber != null ? "agencyNumber=" + agencyNumber + ", " : "") +
            (accountNumbers != null ? "accountNumbers=" + accountNumbers + ", " : "") +
            (userLevel != null ? "userLevel=" + userLevel + ", " : "") +
            (copySubscriberId != null ? "copySubscriberId=" + copySubscriberId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
