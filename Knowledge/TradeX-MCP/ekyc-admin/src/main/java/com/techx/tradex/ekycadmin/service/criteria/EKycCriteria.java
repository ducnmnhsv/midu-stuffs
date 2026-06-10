package com.techx.tradex.ekycadmin.service.criteria;

import com.techx.tradex.ekycadmin.domain.enumeration.EkycType;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LocalDateFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import tech.jhipster.service.filter.ZonedDateTimeFilter;

/**
 * Criteria class for the {@link com.techx.tradex.ekycadmin.domain.EKyc} entity. This class is used
 * in {@link com.techx.tradex.ekycadmin.web.rest.EKycResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /e-kycs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class EKycCriteria implements Serializable, Criteria {

    /**
     * Class for filtering EkycType
     */
    public static class EkycTypeFilter extends Filter<EkycType> {

        public EkycTypeFilter() {}

        public EkycTypeFilter(EkycTypeFilter filter) {
            super(filter);
        }

        @Override
        public EkycTypeFilter copy() {
            return new EkycTypeFilter(this);
        }
    }

    /**
     * Class for filtering Status
     */
    public static class StatusFilter extends Filter<Status> {

        public StatusFilter() {}

        public StatusFilter(StatusFilter filter) {
            super(filter);
        }

        @Override
        public StatusFilter copy() {
            return new StatusFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter identifierId;

    private StringFilter fullName;

    private StringFilter phoneNo;

    private StringFilter gender;

    private EkycTypeFilter type;

    private LocalDateFilter birthDay;

    private LocalDateFilter expiredDate;

    private LocalDateFilter issueDate;

    private StringFilter issuePlace;

    private StringFilter address;

    private StringFilter occupation;

    private StringFilter homeTown;

    private StringFilter permanentProvince;

    private StringFilter permanentDistrict;

    private StringFilter permanentAddress;

    private StringFilter contactProvince;

    private StringFilter contactDistrict;

    private StringFilter contactAddress;

    private StringFilter email;

    private StringFilter referrerIdName;

    private StringFilter referrerBranch;

    private StringFilter bankAccount;

    private StringFilter accountName;

    private StringFilter bankName;

    private StringFilter branch;

    private StringFilter nationality;

    private StatusFilter status;

    private StringFilter frontImageUrl;

    private StringFilter backImageUrl;

    private StringFilter portraitImageUrl;

    private StringFilter signatureImageUrl;

    private StringFilter tradingCodeImageUrl;

    private BooleanFilter isMargin;

    private DoubleFilter matchingRate;

    private ZonedDateTimeFilter updatedAt;

    private ZonedDateTimeFilter createdAt;

    private StringFilter branchId;

    private StringFilter channelId;

    private StringFilter eKycId;

    private StringFilter taxNumber;

    private BooleanFilter onlineTrading;

    private StringFilter authenMethod;

    private StringFilter otpReceiveMethod;

    private BooleanFilter advancedCashIncluded;

    private StringFilter smsMethod;

    private BooleanFilter emailNotification;

    private StringFilter referral;

    private StringFilter partnerId;

    private StringFilter partnerName;

    private BooleanFilter customerSupport;

    private StringFilter csPartnerId;

    private StringFilter csName;

    private StringFilter accountNumber;

    private StringFilter contractId;

    private StringFilter contractStatus;

    private BooleanFilter fatca;

    private StringFilter contractNo;

    private LongFilter eContractId;

    public EKycCriteria() {}

    public EKycCriteria(EKycCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.identifierId = other.identifierId == null ? null : other.identifierId.copy();
        this.fullName = other.fullName == null ? null : other.fullName.copy();
        this.phoneNo = other.phoneNo == null ? null : other.phoneNo.copy();
        this.gender = other.gender == null ? null : other.gender.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.birthDay = other.birthDay == null ? null : other.birthDay.copy();
        this.expiredDate = other.expiredDate == null ? null : other.expiredDate.copy();
        this.issueDate = other.issueDate == null ? null : other.issueDate.copy();
        this.issuePlace = other.issuePlace == null ? null : other.issuePlace.copy();
        this.address = other.address == null ? null : other.address.copy();
        this.occupation = other.occupation == null ? null : other.occupation.copy();
        this.homeTown = other.homeTown == null ? null : other.homeTown.copy();
        this.permanentProvince = other.permanentProvince == null ? null : other.permanentProvince.copy();
        this.permanentDistrict = other.permanentDistrict == null ? null : other.permanentDistrict.copy();
        this.permanentAddress = other.permanentAddress == null ? null : other.permanentAddress.copy();
        this.contactProvince = other.contactProvince == null ? null : other.contactProvince.copy();
        this.contactDistrict = other.contactDistrict == null ? null : other.contactDistrict.copy();
        this.contactAddress = other.contactAddress == null ? null : other.contactAddress.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.referrerIdName = other.referrerIdName == null ? null : other.referrerIdName.copy();
        this.referrerBranch = other.referrerBranch == null ? null : other.referrerBranch.copy();
        this.bankAccount = other.bankAccount == null ? null : other.bankAccount.copy();
        this.accountName = other.accountName == null ? null : other.accountName.copy();
        this.bankName = other.bankName == null ? null : other.bankName.copy();
        this.branch = other.branch == null ? null : other.branch.copy();
        this.nationality = other.nationality == null ? null : other.nationality.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.frontImageUrl = other.frontImageUrl == null ? null : other.frontImageUrl.copy();
        this.backImageUrl = other.backImageUrl == null ? null : other.backImageUrl.copy();
        this.portraitImageUrl = other.portraitImageUrl == null ? null : other.portraitImageUrl.copy();
        this.signatureImageUrl = other.signatureImageUrl == null ? null : other.signatureImageUrl.copy();
        this.tradingCodeImageUrl = other.tradingCodeImageUrl == null ? null : other.tradingCodeImageUrl.copy();
        this.isMargin = other.isMargin == null ? null : other.isMargin.copy();
        this.matchingRate = other.matchingRate == null ? null : other.matchingRate.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.branchId = other.branchId == null ? null : other.branchId.copy();
        this.channelId = other.channelId == null ? null : other.channelId.copy();
        this.eKycId = other.eKycId == null ? null : other.eKycId.copy();
        this.taxNumber = other.taxNumber == null ? null : other.taxNumber.copy();
        this.onlineTrading = other.onlineTrading == null ? null : other.onlineTrading.copy();
        this.authenMethod = other.authenMethod == null ? null : other.authenMethod.copy();
        this.otpReceiveMethod = other.otpReceiveMethod == null ? null : other.otpReceiveMethod.copy();
        this.advancedCashIncluded = other.advancedCashIncluded == null ? null : other.advancedCashIncluded.copy();
        this.smsMethod = other.smsMethod == null ? null : other.smsMethod.copy();
        this.emailNotification = other.emailNotification == null ? null : other.emailNotification.copy();
        this.referral = other.referral == null ? null : other.referral.copy();
        this.partnerId = other.partnerId == null ? null : other.partnerId.copy();
        this.partnerName = other.partnerName == null ? null : other.partnerName.copy();
        this.customerSupport = other.customerSupport == null ? null : other.customerSupport.copy();
        this.csPartnerId = other.csPartnerId == null ? null : other.csPartnerId.copy();
        this.csName = other.csName == null ? null : other.csName.copy();
        this.accountNumber = other.accountNumber == null ? null : other.accountNumber.copy();
        this.contractId = other.contractId == null ? null : other.contractId.copy();
        this.contractStatus = other.contractStatus == null ? null : other.contractStatus.copy();
        this.fatca = other.fatca == null ? null : other.fatca.copy();
        this.contractNo = other.contractNo == null ? null : other.contractNo.copy();
        this.eContractId = other.eContractId == null ? null : other.eContractId.copy();
    }

    @Override
    public EKycCriteria copy() {
        return new EKycCriteria(this);
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

    public StringFilter getFullName() {
        return fullName;
    }

    public StringFilter fullName() {
        if (fullName == null) {
            fullName = new StringFilter();
        }
        return fullName;
    }

    public void setFullName(StringFilter fullName) {
        this.fullName = fullName;
    }

    public StringFilter getPhoneNo() {
        return phoneNo;
    }

    public StringFilter phoneNo() {
        if (phoneNo == null) {
            phoneNo = new StringFilter();
        }
        return phoneNo;
    }

    public void setPhoneNo(StringFilter phoneNo) {
        this.phoneNo = phoneNo;
    }

    public StringFilter getGender() {
        return gender;
    }

    public StringFilter gender() {
        if (gender == null) {
            gender = new StringFilter();
        }
        return gender;
    }

    public void setGender(StringFilter gender) {
        this.gender = gender;
    }

    public EkycTypeFilter getType() {
        return type;
    }

    public EkycTypeFilter type() {
        if (type == null) {
            type = new EkycTypeFilter();
        }
        return type;
    }

    public void setType(EkycTypeFilter type) {
        this.type = type;
    }

    public LocalDateFilter getBirthDay() {
        return birthDay;
    }

    public LocalDateFilter birthDay() {
        if (birthDay == null) {
            birthDay = new LocalDateFilter();
        }
        return birthDay;
    }

    public void setBirthDay(LocalDateFilter birthDay) {
        this.birthDay = birthDay;
    }

    public LocalDateFilter getExpiredDate() {
        return expiredDate;
    }

    public LocalDateFilter expiredDate() {
        if (expiredDate == null) {
            expiredDate = new LocalDateFilter();
        }
        return expiredDate;
    }

    public void setExpiredDate(LocalDateFilter expiredDate) {
        this.expiredDate = expiredDate;
    }

    public LocalDateFilter getIssueDate() {
        return issueDate;
    }

    public LocalDateFilter issueDate() {
        if (issueDate == null) {
            issueDate = new LocalDateFilter();
        }
        return issueDate;
    }

    public void setIssueDate(LocalDateFilter issueDate) {
        this.issueDate = issueDate;
    }

    public StringFilter getIssuePlace() {
        return issuePlace;
    }

    public StringFilter issuePlace() {
        if (issuePlace == null) {
            issuePlace = new StringFilter();
        }
        return issuePlace;
    }

    public void setIssuePlace(StringFilter issuePlace) {
        this.issuePlace = issuePlace;
    }

    public StringFilter getAddress() {
        return address;
    }

    public StringFilter address() {
        if (address == null) {
            address = new StringFilter();
        }
        return address;
    }

    public void setAddress(StringFilter address) {
        this.address = address;
    }

    public StringFilter getOccupation() {
        return occupation;
    }

    public StringFilter occupation() {
        if (occupation == null) {
            occupation = new StringFilter();
        }
        return occupation;
    }

    public void setOccupation(StringFilter occupation) {
        this.occupation = occupation;
    }

    public StringFilter getHomeTown() {
        return homeTown;
    }

    public StringFilter homeTown() {
        if (homeTown == null) {
            homeTown = new StringFilter();
        }
        return homeTown;
    }

    public void setHomeTown(StringFilter homeTown) {
        this.homeTown = homeTown;
    }

    public StringFilter getPermanentProvince() {
        return permanentProvince;
    }

    public StringFilter permanentProvince() {
        if (permanentProvince == null) {
            permanentProvince = new StringFilter();
        }
        return permanentProvince;
    }

    public void setPermanentProvince(StringFilter permanentProvince) {
        this.permanentProvince = permanentProvince;
    }

    public StringFilter getPermanentDistrict() {
        return permanentDistrict;
    }

    public StringFilter permanentDistrict() {
        if (permanentDistrict == null) {
            permanentDistrict = new StringFilter();
        }
        return permanentDistrict;
    }

    public void setPermanentDistrict(StringFilter permanentDistrict) {
        this.permanentDistrict = permanentDistrict;
    }

    public StringFilter getPermanentAddress() {
        return permanentAddress;
    }

    public StringFilter permanentAddress() {
        if (permanentAddress == null) {
            permanentAddress = new StringFilter();
        }
        return permanentAddress;
    }

    public void setPermanentAddress(StringFilter permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public StringFilter getContactProvince() {
        return contactProvince;
    }

    public StringFilter contactProvince() {
        if (contactProvince == null) {
            contactProvince = new StringFilter();
        }
        return contactProvince;
    }

    public void setContactProvince(StringFilter contactProvince) {
        this.contactProvince = contactProvince;
    }

    public StringFilter getContactDistrict() {
        return contactDistrict;
    }

    public StringFilter contactDistrict() {
        if (contactDistrict == null) {
            contactDistrict = new StringFilter();
        }
        return contactDistrict;
    }

    public void setContactDistrict(StringFilter contactDistrict) {
        this.contactDistrict = contactDistrict;
    }

    public StringFilter getContactAddress() {
        return contactAddress;
    }

    public StringFilter contactAddress() {
        if (contactAddress == null) {
            contactAddress = new StringFilter();
        }
        return contactAddress;
    }

    public void setContactAddress(StringFilter contactAddress) {
        this.contactAddress = contactAddress;
    }

    public StringFilter getEmail() {
        return email;
    }

    public StringFilter email() {
        if (email == null) {
            email = new StringFilter();
        }
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
    }

    public StringFilter getReferrerIdName() {
        return referrerIdName;
    }

    public StringFilter referrerIdName() {
        if (referrerIdName == null) {
            referrerIdName = new StringFilter();
        }
        return referrerIdName;
    }

    public void setReferrerIdName(StringFilter referrerIdName) {
        this.referrerIdName = referrerIdName;
    }

    public StringFilter getReferrerBranch() {
        return referrerBranch;
    }

    public StringFilter referrerBranch() {
        if (referrerBranch == null) {
            referrerBranch = new StringFilter();
        }
        return referrerBranch;
    }

    public void setReferrerBranch(StringFilter referrerBranch) {
        this.referrerBranch = referrerBranch;
    }

    public StringFilter getBankAccount() {
        return bankAccount;
    }

    public StringFilter bankAccount() {
        if (bankAccount == null) {
            bankAccount = new StringFilter();
        }
        return bankAccount;
    }

    public void setBankAccount(StringFilter bankAccount) {
        this.bankAccount = bankAccount;
    }

    public StringFilter getAccountName() {
        return accountName;
    }

    public StringFilter accountName() {
        if (accountName == null) {
            accountName = new StringFilter();
        }
        return accountName;
    }

    public void setAccountName(StringFilter accountName) {
        this.accountName = accountName;
    }

    public StringFilter getBankName() {
        return bankName;
    }

    public StringFilter bankName() {
        if (bankName == null) {
            bankName = new StringFilter();
        }
        return bankName;
    }

    public void setBankName(StringFilter bankName) {
        this.bankName = bankName;
    }

    public StringFilter getBranch() {
        return branch;
    }

    public StringFilter branch() {
        if (branch == null) {
            branch = new StringFilter();
        }
        return branch;
    }

    public void setBranch(StringFilter branch) {
        this.branch = branch;
    }

    public StringFilter getNationality() {
        return nationality;
    }

    public StringFilter nationality() {
        if (nationality == null) {
            nationality = new StringFilter();
        }
        return nationality;
    }

    public void setNationality(StringFilter nationality) {
        this.nationality = nationality;
    }

    public StatusFilter getStatus() {
        return status;
    }

    public StatusFilter status() {
        if (status == null) {
            status = new StatusFilter();
        }
        return status;
    }

    public void setStatus(StatusFilter status) {
        this.status = status;
    }

    public StringFilter getFrontImageUrl() {
        return frontImageUrl;
    }

    public StringFilter frontImageUrl() {
        if (frontImageUrl == null) {
            frontImageUrl = new StringFilter();
        }
        return frontImageUrl;
    }

    public void setFrontImageUrl(StringFilter frontImageUrl) {
        this.frontImageUrl = frontImageUrl;
    }

    public StringFilter getBackImageUrl() {
        return backImageUrl;
    }

    public StringFilter backImageUrl() {
        if (backImageUrl == null) {
            backImageUrl = new StringFilter();
        }
        return backImageUrl;
    }

    public void setBackImageUrl(StringFilter backImageUrl) {
        this.backImageUrl = backImageUrl;
    }

    public StringFilter getPortraitImageUrl() {
        return portraitImageUrl;
    }

    public StringFilter portraitImageUrl() {
        if (portraitImageUrl == null) {
            portraitImageUrl = new StringFilter();
        }
        return portraitImageUrl;
    }

    public void setPortraitImageUrl(StringFilter portraitImageUrl) {
        this.portraitImageUrl = portraitImageUrl;
    }

    public StringFilter getSignatureImageUrl() {
        return signatureImageUrl;
    }

    public StringFilter signatureImageUrl() {
        if (signatureImageUrl == null) {
            signatureImageUrl = new StringFilter();
        }
        return signatureImageUrl;
    }

    public void setSignatureImageUrl(StringFilter signatureImageUrl) {
        this.signatureImageUrl = signatureImageUrl;
    }

    public StringFilter getTradingCodeImageUrl() {
        return tradingCodeImageUrl;
    }

    public StringFilter tradingCodeImageUrl() {
        if (tradingCodeImageUrl == null) {
            tradingCodeImageUrl = new StringFilter();
        }
        return tradingCodeImageUrl;
    }

    public void setTradingCodeImageUrl(StringFilter tradingCodeImageUrl) {
        this.tradingCodeImageUrl = tradingCodeImageUrl;
    }

    public BooleanFilter getIsMargin() {
        return isMargin;
    }

    public BooleanFilter isMargin() {
        if (isMargin == null) {
            isMargin = new BooleanFilter();
        }
        return isMargin;
    }

    public void setIsMargin(BooleanFilter isMargin) {
        this.isMargin = isMargin;
    }

    public DoubleFilter getMatchingRate() {
        return matchingRate;
    }

    public DoubleFilter matchingRate() {
        if (matchingRate == null) {
            matchingRate = new DoubleFilter();
        }
        return matchingRate;
    }

    public void setMatchingRate(DoubleFilter matchingRate) {
        this.matchingRate = matchingRate;
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

    public StringFilter getBranchId() {
        return branchId;
    }

    public StringFilter branchId() {
        if (branchId == null) {
            branchId = new StringFilter();
        }
        return branchId;
    }

    public void setBranchId(StringFilter branchId) {
        this.branchId = branchId;
    }

    public StringFilter getChannelId() {
        return channelId;
    }

    public StringFilter channelId() {
        if (channelId == null) {
            channelId = new StringFilter();
        }
        return channelId;
    }

    public void setChannelId(StringFilter channelId) {
        this.channelId = channelId;
    }

    public StringFilter geteKycId() {
        return eKycId;
    }

    public StringFilter eKycId() {
        if (eKycId == null) {
            eKycId = new StringFilter();
        }
        return eKycId;
    }

    public void seteKycId(StringFilter eKycId) {
        this.eKycId = eKycId;
    }

    public StringFilter getTaxNumber() {
        return taxNumber;
    }

    public StringFilter taxNumber() {
        if (taxNumber == null) {
            taxNumber = new StringFilter();
        }
        return taxNumber;
    }

    public void setTaxNumber(StringFilter taxNumber) {
        this.taxNumber = taxNumber;
    }

    public BooleanFilter getOnlineTrading() {
        return onlineTrading;
    }

    public BooleanFilter onlineTrading() {
        if (onlineTrading == null) {
            onlineTrading = new BooleanFilter();
        }
        return onlineTrading;
    }

    public void setOnlineTrading(BooleanFilter onlineTrading) {
        this.onlineTrading = onlineTrading;
    }

    public StringFilter getAuthenMethod() {
        return authenMethod;
    }

    public StringFilter authenMethod() {
        if (authenMethod == null) {
            authenMethod = new StringFilter();
        }
        return authenMethod;
    }

    public void setAuthenMethod(StringFilter authenMethod) {
        this.authenMethod = authenMethod;
    }

    public StringFilter getOtpReceiveMethod() {
        return otpReceiveMethod;
    }

    public StringFilter otpReceiveMethod() {
        if (otpReceiveMethod == null) {
            otpReceiveMethod = new StringFilter();
        }
        return otpReceiveMethod;
    }

    public void setOtpReceiveMethod(StringFilter otpReceiveMethod) {
        this.otpReceiveMethod = otpReceiveMethod;
    }

    public BooleanFilter getAdvancedCashIncluded() {
        return advancedCashIncluded;
    }

    public BooleanFilter advancedCashIncluded() {
        if (advancedCashIncluded == null) {
            advancedCashIncluded = new BooleanFilter();
        }
        return advancedCashIncluded;
    }

    public void setAdvancedCashIncluded(BooleanFilter advancedCashIncluded) {
        this.advancedCashIncluded = advancedCashIncluded;
    }

    public StringFilter getSmsMethod() {
        return smsMethod;
    }

    public StringFilter smsMethod() {
        if (smsMethod == null) {
            smsMethod = new StringFilter();
        }
        return smsMethod;
    }

    public void setSmsMethod(StringFilter smsMethod) {
        this.smsMethod = smsMethod;
    }

    public BooleanFilter getEmailNotification() {
        return emailNotification;
    }

    public BooleanFilter emailNotification() {
        if (emailNotification == null) {
            emailNotification = new BooleanFilter();
        }
        return emailNotification;
    }

    public void setEmailNotification(BooleanFilter emailNotification) {
        this.emailNotification = emailNotification;
    }

    public StringFilter getReferral() {
        return referral;
    }

    public StringFilter referral() {
        if (referral == null) {
            referral = new StringFilter();
        }
        return referral;
    }

    public void setReferral(StringFilter referral) {
        this.referral = referral;
    }

    public StringFilter getPartnerId() {
        return partnerId;
    }

    public StringFilter partnerId() {
        if (partnerId == null) {
            partnerId = new StringFilter();
        }
        return partnerId;
    }

    public void setPartnerId(StringFilter partnerId) {
        this.partnerId = partnerId;
    }

    public StringFilter getPartnerName() {
        return partnerName;
    }

    public StringFilter partnerName() {
        if (partnerName == null) {
            partnerName = new StringFilter();
        }
        return partnerName;
    }

    public void setPartnerName(StringFilter partnerName) {
        this.partnerName = partnerName;
    }

    public BooleanFilter getCustomerSupport() {
        return customerSupport;
    }

    public BooleanFilter customerSupport() {
        if (customerSupport == null) {
            customerSupport = new BooleanFilter();
        }
        return customerSupport;
    }

    public void setCustomerSupport(BooleanFilter customerSupport) {
        this.customerSupport = customerSupport;
    }

    public StringFilter getCsPartnerId() {
        return csPartnerId;
    }

    public StringFilter csPartnerId() {
        if (csPartnerId == null) {
            csPartnerId = new StringFilter();
        }
        return csPartnerId;
    }

    public void setCsPartnerId(StringFilter csPartnerId) {
        this.csPartnerId = csPartnerId;
    }

    public StringFilter getCsName() {
        return csName;
    }

    public StringFilter csName() {
        if (csName == null) {
            csName = new StringFilter();
        }
        return csName;
    }

    public void setCsName(StringFilter csName) {
        this.csName = csName;
    }

    public StringFilter getAccountNumber() {
        return accountNumber;
    }

    public StringFilter accountNumber() {
        if (accountNumber == null) {
            accountNumber = new StringFilter();
        }
        return accountNumber;
    }

    public void setAccountNumber(StringFilter accountNumber) {
        this.accountNumber = accountNumber;
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

    public StringFilter getContractId() {
        return contractId;
    }

    public StringFilter contractId() {
        if (contractId == null) {
            contractId = new StringFilter();
        }
        return contractId;
    }

    public void setContractId(StringFilter contractId) {
        this.contractId = contractId;
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

    public BooleanFilter getFatca() {
        return fatca;
    }

    public BooleanFilter fatca() {
        if (fatca == null) {
            fatca = new BooleanFilter();
        }
        return fatca;
    }

    public void setFatca(BooleanFilter fatca) {
        this.fatca = fatca;
    }

    public StringFilter getContractNo() {
        return contractNo;
    }

    public StringFilter contractNo() {
        if (contractNo == null) {
            contractNo = new StringFilter();
        }
        return contractNo;
    }

    public void setContractNo(StringFilter contractNo) {
        this.contractNo = contractNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EKycCriteria that = (EKycCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(identifierId, that.identifierId) &&
            Objects.equals(fullName, that.fullName) &&
            Objects.equals(phoneNo, that.phoneNo) &&
            Objects.equals(gender, that.gender) &&
            Objects.equals(type, that.type) &&
            Objects.equals(birthDay, that.birthDay) &&
            Objects.equals(expiredDate, that.expiredDate) &&
            Objects.equals(issueDate, that.issueDate) &&
            Objects.equals(issuePlace, that.issuePlace) &&
            Objects.equals(address, that.address) &&
            Objects.equals(occupation, that.occupation) &&
            Objects.equals(homeTown, that.homeTown) &&
            Objects.equals(permanentProvince, that.permanentProvince) &&
            Objects.equals(permanentDistrict, that.permanentDistrict) &&
            Objects.equals(permanentAddress, that.permanentAddress) &&
            Objects.equals(contactProvince, that.contactProvince) &&
            Objects.equals(contactDistrict, that.contactDistrict) &&
            Objects.equals(contactAddress, that.contactAddress) &&
            Objects.equals(email, that.email) &&
            Objects.equals(referrerIdName, that.referrerIdName) &&
            Objects.equals(referrerBranch, that.referrerBranch) &&
            Objects.equals(bankAccount, that.bankAccount) &&
            Objects.equals(accountName, that.accountName) &&
            Objects.equals(bankName, that.bankName) &&
            Objects.equals(branch, that.branch) &&
            Objects.equals(nationality, that.nationality) &&
            Objects.equals(status, that.status) &&
            Objects.equals(frontImageUrl, that.frontImageUrl) &&
            Objects.equals(backImageUrl, that.backImageUrl) &&
            Objects.equals(portraitImageUrl, that.portraitImageUrl) &&
            Objects.equals(signatureImageUrl, that.signatureImageUrl) &&
            Objects.equals(tradingCodeImageUrl, that.tradingCodeImageUrl) &&
            Objects.equals(isMargin, that.isMargin) &&
            Objects.equals(matchingRate, that.matchingRate) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(branchId, that.branchId) &&
            Objects.equals(channelId, that.channelId) &&
            Objects.equals(eContractId, that.eContractId) &&
            Objects.equals(eKycId, that.eKycId) &&
            Objects.equals(taxNumber, that.taxNumber) &&
            Objects.equals(onlineTrading, that.onlineTrading) &&
            Objects.equals(authenMethod, that.authenMethod) &&
            Objects.equals(otpReceiveMethod, that.otpReceiveMethod) &&
            Objects.equals(advancedCashIncluded, that.advancedCashIncluded) &&
            Objects.equals(smsMethod, that.smsMethod) &&
            Objects.equals(emailNotification, that.emailNotification) &&
            Objects.equals(referral, that.referral) &&
            Objects.equals(partnerId, that.partnerId) &&
            Objects.equals(partnerName, that.partnerName) &&
            Objects.equals(customerSupport, that.customerSupport) &&
            Objects.equals(csPartnerId, that.csPartnerId) &&
            Objects.equals(csName, that.csName) &&
            Objects.equals(accountNumber, that.accountNumber) &&
            Objects.equals(contractId, that.contractId) &&
            Objects.equals(contractStatus, that.contractStatus) &&
            Objects.equals(fatca, that.fatca) &&
            Objects.equals(contractNo, that.contractNo)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            identifierId,
            fullName,
            phoneNo,
            gender,
            type,
            birthDay,
            expiredDate,
            issueDate,
            issuePlace,
            address,
            occupation,
            homeTown,
            permanentProvince,
            permanentDistrict,
            permanentAddress,
            contactProvince,
            contactDistrict,
            contactAddress,
            email,
            referrerIdName,
            referrerBranch,
            bankAccount,
            accountName,
            bankName,
            branch,
            nationality,
            status,
            frontImageUrl,
            backImageUrl,
            portraitImageUrl,
            signatureImageUrl,
            tradingCodeImageUrl,
            isMargin,
            matchingRate,
            updatedAt,
            createdAt,
            branchId,
            channelId,
            eKycId,
            taxNumber,
            onlineTrading,
            authenMethod,
            otpReceiveMethod,
            advancedCashIncluded,
            smsMethod,
            emailNotification,
            referral,
            partnerId,
            partnerName,
            customerSupport,
            csPartnerId,
            csName,
            accountNumber,
            contractId,
            contractStatus,
            fatca,
            contractNo,
            eContractId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EKycCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (identifierId != null ? "identifierId=" + identifierId + ", " : "") +
            (fullName != null ? "fullName=" + fullName + ", " : "") +
            (phoneNo != null ? "phoneNo=" + phoneNo + ", " : "") +
            (gender != null ? "gender=" + gender + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (birthDay != null ? "birthDay=" + birthDay + ", " : "") +
            (expiredDate != null ? "expiredDate=" + expiredDate + ", " : "") +
            (issueDate != null ? "issueDate=" + issueDate + ", " : "") +
            (issuePlace != null ? "issuePlace=" + issuePlace + ", " : "") +
            (address != null ? "address=" + address + ", " : "") +
            (occupation != null ? "occupation=" + occupation + ", " : "") +
            (homeTown != null ? "homeTown=" + homeTown + ", " : "") +
            (permanentProvince != null ? "permanentProvince=" + permanentProvince + ", " : "") +
            (permanentDistrict != null ? "permanentDistrict=" + permanentDistrict + ", " : "") +
            (permanentAddress != null ? "permanentAddress=" + permanentAddress + ", " : "") +
            (contactProvince != null ? "contactProvince=" + contactProvince + ", " : "") +
            (contactDistrict != null ? "contactDistrict=" + contactDistrict + ", " : "") +
            (contactAddress != null ? "contactAddress=" + contactAddress + ", " : "") +
            (email != null ? "email=" + email + ", " : "") +
            (referrerIdName != null ? "referrerIdName=" + referrerIdName + ", " : "") +
            (referrerBranch != null ? "referrerBranch=" + referrerBranch + ", " : "") +
            (bankAccount != null ? "bankAccount=" + bankAccount + ", " : "") +
            (accountName != null ? "accountName=" + accountName + ", " : "") +
            (bankName != null ? "bankName=" + bankName + ", " : "") +
            (branch != null ? "branch=" + branch + ", " : "") +
            (nationality != null ? "nationality=" + nationality + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (frontImageUrl != null ? "frontImageUrl=" + frontImageUrl + ", " : "") +
            (backImageUrl != null ? "backImageUrl=" + backImageUrl + ", " : "") +
            (portraitImageUrl != null ? "portraitImageUrl=" + portraitImageUrl + ", " : "") +
            (signatureImageUrl != null ? "signatureImageUrl=" + signatureImageUrl + ", " : "") +
            (tradingCodeImageUrl != null ? "tradingCodeImageUrl=" + tradingCodeImageUrl + ", " : "") +
            (isMargin != null ? "isMargin=" + isMargin + ", " : "") +
            (matchingRate != null ? "matchingRate=" + matchingRate + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (branchId != null ? "branchId=" + branchId + ", " : "") +
            (channelId != null ? "channelId=" + channelId + ", " : "") +
            (eKycId != null ? "eKycId=" + eKycId + ", " : "") +
            (taxNumber != null ? "taxNumber=" + taxNumber + ", " : "") +
            (onlineTrading != null ? "onlineTrading=" + onlineTrading + ", " : "") +
            (authenMethod != null ? "authenMethod=" + authenMethod + ", " : "") +
            (otpReceiveMethod != null ? "otpReceiveMethod=" + otpReceiveMethod + ", " : "") +
            (advancedCashIncluded != null ? "advancedCashIncluded=" + advancedCashIncluded + ", " : "") +
            (smsMethod != null ? "smsMethod=" + smsMethod + ", " : "") +
            (emailNotification != null ? "emailNotification=" + emailNotification + ", " : "") +
            (referral != null ? "referral=" + referral + ", " : "") +
            (partnerId != null ? "partnerId=" + partnerId + ", " : "") +
            (partnerName != null ? "partnerName=" + partnerName + ", " : "") +
            (customerSupport != null ? "customerSupport=" + customerSupport + ", " : "") +
            (csPartnerId != null ? "csPartnerId=" + csPartnerId + ", " : "") +
            (csName != null ? "csName=" + csName + ", " : "") +
            (accountNumber != null ? "accountNumber=" + accountNumber + ", " : "") +
            (eContractId != null ? "eContractId=" + eContractId + ", " : "") +
            (contractId != null ? "contractId=" + contractId + ", " : "") +
            (contractStatus != null ? "contractStatus=" + contractStatus + ", " : "") +
            (fatca != null ? "fatca=" + fatca + ", " : "") +
            (contractNo != null ? "contractNo=" + contractNo + ", " : "") +
            "}";
    }
}
