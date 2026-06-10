package com.techx.tradex.ekycadmin.domain;

import com.techx.tradex.ekycadmin.domain.enumeration.EkycType;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A EKyc.
 */
@Entity
@Table(name = "e_kyc")
public class EKyc implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "identifier_id", length = 20, nullable = false)
    private String identifierId;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "full_name", length = 50, nullable = false)
    private String fullName;

    @Pattern(regexp = "^[+]{0,1}[-\\s0-9]*$")
    @Column(name = "phone_no")
    private String phoneNo;

    @Column(name = "gender")
    private String gender;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EkycType type;

    @NotNull
    @Column(name = "birth_day", nullable = false)
    private LocalDate birthDay;

    @NotNull
    @Column(name = "expired_date", nullable = false)
    private LocalDate expiredDate;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "issue_place", length = 100, nullable = false)
    private String issuePlace;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "address", length = 100, nullable = false)
    private String address;

    @Column(name = "occupation")
    private String occupation;

    @Size(min = 1, max = 100)
    @Column(name = "home_town", length = 100)
    private String homeTown;

    @Column(name = "permanent_province")
    private String permanentProvince;

    @Column(name = "permanent_district")
    private String permanentDistrict;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(name = "contact_province")
    private String contactProvince;

    @Column(name = "contact_district")
    private String contactDistrict;

    @Column(name = "contact_address")
    private String contactAddress;

    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "referrer_id_name")
    private String referrerIdName;

    @Column(name = "referrer_branch")
    private String referrerBranch;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "branch")
    private String branch;

    @Column(name = "nationality")
    private String nationality;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @NotNull
    @Column(name = "front_image_url", nullable = false)
    private String frontImageUrl;

    @NotNull
    @Column(name = "back_image_url", nullable = false)
    private String backImageUrl;

    @Column(name = "portrait_image_url")
    private String portraitImageUrl;

    @Column(name = "signature_image_url")
    private String signatureImageUrl;

    @Column(name = "trading_code_image_url")
    private String tradingCodeImageUrl;

    @Column(name = "is_margin")
    private Boolean isMargin;

    @Column(name = "matching_rate")
    private Double matchingRate;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "branch_id")
    private String branchId;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "e_kyc_id")
    private String eKycId;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "online_trading")
    private Boolean onlineTrading;

    @Column(name = "authen_method")
    private String authenMethod;

    @Column(name = "otp_receive_method")
    private String otpReceiveMethod;

    @Column(name = "advanced_cash_included")
    private Boolean advancedCashIncluded;

    @Column(name = "sms_method")
    private String smsMethod;

    @Column(name = "email_notification")
    private Boolean emailNotification;

    @Column(name = "referral")
    private String referral;

    @Column(name = "partner_id")
    private String partnerId;

    @Column(name = "partner_name")
    private String partnerName;

    @Column(name = "customer_support")
    private Boolean customerSupport;

    @Column(name = "cs_partner_id")
    private String csPartnerId;

    @Column(name = "cs_name")
    private String csName;

    @Column(name = "contract_id")
    private String contractId;

    @Column(name = "contract_status")
    private String contractStatus;

    @Column(name = "fatca")
    private Boolean fatca;

    @Size(max = 255)
    @Column(name = "contract_no", length = 255)
    private String contractNo;

    @Size(max = 255)
    @Column(name = "account_number", length = 255)
    private String accountNumber;

    @Column(name = "ocr_log_id")
    private String ocrLogId;

    @Column(name = "card_liveness_log_id")
    private String cardLivenessLogId;

    @Column(name = "card_rear_log_id")
    private String cardRearLogId;

    @Column(name = "compare_log_id")
    private String compareLogId;

    @Column(name = "face_liveness_log_id")
    private String faceLivenessLogId;

    @Column(name = "face_mask_log_id")
    private String faceMaskLogId;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EKyc id(Long id) {
        this.id = id;
        return this;
    }

    public String getIdentifierId() {
        return this.identifierId;
    }

    public EKyc identifierId(String identifierId) {
        this.identifierId = identifierId;
        return this;
    }

    public void setIdentifierId(String identifierId) {
        this.identifierId = identifierId;
    }

    public String getFullName() {
        return this.fullName;
    }

    public EKyc fullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNo() {
        return this.phoneNo;
    }

    public EKyc phoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
        return this;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getGender() {
        return this.gender;
    }

    public EKyc gender(String gender) {
        this.gender = gender;
        return this;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public EkycType getType() {
        return this.type;
    }

    public EKyc type(EkycType type) {
        this.type = type;
        return this;
    }

    public void setType(EkycType type) {
        this.type = type;
    }

    public LocalDate getBirthDay() {
        return this.birthDay;
    }

    public EKyc birthDay(LocalDate birthDay) {
        this.birthDay = birthDay;
        return this;
    }

    public void setBirthDay(LocalDate birthDay) {
        this.birthDay = birthDay;
    }

    public LocalDate getExpiredDate() {
        return this.expiredDate;
    }

    public EKyc expiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
        return this;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
    }

    public LocalDate getIssueDate() {
        return this.issueDate;
    }

    public EKyc issueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getIssuePlace() {
        return this.issuePlace;
    }

    public EKyc issuePlace(String issuePlace) {
        this.issuePlace = issuePlace;
        return this;
    }

    public void setIssuePlace(String issuePlace) {
        this.issuePlace = issuePlace;
    }

    public String getAddress() {
        return this.address;
    }

    public EKyc address(String address) {
        this.address = address;
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOccupation() {
        return this.occupation;
    }

    public EKyc occupation(String occupation) {
        this.occupation = occupation;
        return this;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getHomeTown() {
        return this.homeTown;
    }

    public EKyc homeTown(String homeTown) {
        this.homeTown = homeTown;
        return this;
    }

    public void setHomeTown(String homeTown) {
        this.homeTown = homeTown;
    }

    public String getPermanentProvince() {
        return this.permanentProvince;
    }

    public EKyc permanentProvince(String permanentProvince) {
        this.permanentProvince = permanentProvince;
        return this;
    }

    public void setPermanentProvince(String permanentProvince) {
        this.permanentProvince = permanentProvince;
    }

    public String getPermanentDistrict() {
        return this.permanentDistrict;
    }

    public EKyc permanentDistrict(String permanentDistrict) {
        this.permanentDistrict = permanentDistrict;
        return this;
    }

    public void setPermanentDistrict(String permanentDistrict) {
        this.permanentDistrict = permanentDistrict;
    }

    public String getPermanentAddress() {
        return this.permanentAddress;
    }

    public EKyc permanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
        return this;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getContactProvince() {
        return this.contactProvince;
    }

    public EKyc contactProvince(String contactProvince) {
        this.contactProvince = contactProvince;
        return this;
    }

    public void setContactProvince(String contactProvince) {
        this.contactProvince = contactProvince;
    }

    public String getContactDistrict() {
        return this.contactDistrict;
    }

    public EKyc contactDistrict(String contactDistrict) {
        this.contactDistrict = contactDistrict;
        return this;
    }

    public void setContactDistrict(String contactDistrict) {
        this.contactDistrict = contactDistrict;
    }

    public String getContactAddress() {
        return this.contactAddress;
    }

    public EKyc contactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
        return this;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public String getEmail() {
        return this.email;
    }

    public EKyc email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReferrerIdName() {
        return this.referrerIdName;
    }

    public EKyc referrerIdName(String referrerIdName) {
        this.referrerIdName = referrerIdName;
        return this;
    }

    public void setReferrerIdName(String referrerIdName) {
        this.referrerIdName = referrerIdName;
    }

    public String getReferrerBranch() {
        return this.referrerBranch;
    }

    public EKyc referrerBranch(String referrerBranch) {
        this.referrerBranch = referrerBranch;
        return this;
    }

    public void setReferrerBranch(String referrerBranch) {
        this.referrerBranch = referrerBranch;
    }

    public String getBankAccount() {
        return this.bankAccount;
    }

    public EKyc bankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
        return this;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public EKyc accountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBankName() {
        return this.bankName;
    }

    public EKyc bankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranch() {
        return this.branch;
    }

    public EKyc branch(String branch) {
        this.branch = branch;
        return this;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getNationality() {
        return this.nationality;
    }

    public EKyc nationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Status getStatus() {
        return this.status;
    }

    public EKyc status(Status status) {
        this.status = status;
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getFrontImageUrl() {
        return this.frontImageUrl;
    }

    public EKyc frontImageUrl(String frontImageUrl) {
        this.frontImageUrl = frontImageUrl;
        return this;
    }

    public void setFrontImageUrl(String frontImageUrl) {
        this.frontImageUrl = frontImageUrl;
    }

    public String getBackImageUrl() {
        return this.backImageUrl;
    }

    public EKyc backImageUrl(String backImageUrl) {
        this.backImageUrl = backImageUrl;
        return this;
    }

    public void setBackImageUrl(String backImageUrl) {
        this.backImageUrl = backImageUrl;
    }

    public String getPortraitImageUrl() {
        return this.portraitImageUrl;
    }

    public EKyc portraitImageUrl(String portraitImageUrl) {
        this.portraitImageUrl = portraitImageUrl;
        return this;
    }

    public void setPortraitImageUrl(String portraitImageUrl) {
        this.portraitImageUrl = portraitImageUrl;
    }

    public String getSignatureImageUrl() {
        return this.signatureImageUrl;
    }

    public EKyc signatureImageUrl(String signatureImageUrl) {
        this.signatureImageUrl = signatureImageUrl;
        return this;
    }

    public void setSignatureImageUrl(String signatureImageUrl) {
        this.signatureImageUrl = signatureImageUrl;
    }

    public String getTradingCodeImageUrl() {
        return this.tradingCodeImageUrl;
    }

    public EKyc tradingCodeImageUrl(String tradingCodeImageUrl) {
        this.tradingCodeImageUrl = tradingCodeImageUrl;
        return this;
    }

    public void setTradingCodeImageUrl(String tradingCodeImageUrl) {
        this.tradingCodeImageUrl = tradingCodeImageUrl;
    }

    public Boolean getIsMargin() {
        return this.isMargin;
    }

    public EKyc isMargin(Boolean isMargin) {
        this.isMargin = isMargin;
        return this;
    }

    public void setIsMargin(Boolean isMargin) {
        this.isMargin = isMargin;
    }

    public Double getMatchingRate() {
        return this.matchingRate;
    }

    public EKyc matchingRate(Double matchingRate) {
        this.matchingRate = matchingRate;
        return this;
    }

    public void setMatchingRate(Double matchingRate) {
        this.matchingRate = matchingRate;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public EKyc updatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public EKyc createdAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getBranchId() {
        return this.branchId;
    }

    public EKyc branchId(String branchId) {
        this.branchId = branchId;
        return this;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public EKyc channelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String geteKycId() {
        return this.eKycId;
    }

    public EKyc eKycId(String eKycId) {
        this.eKycId = eKycId;
        return this;
    }

    public void seteKycId(String eKycId) {
        this.eKycId = eKycId;
    }

    public String getTaxNumber() {
        return this.taxNumber;
    }

    public EKyc taxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
        return this;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public Boolean getOnlineTrading() {
        return this.onlineTrading;
    }

    public EKyc onlineTrading(Boolean onlineTrading) {
        this.onlineTrading = onlineTrading;
        return this;
    }

    public void setOnlineTrading(Boolean onlineTrading) {
        this.onlineTrading = onlineTrading;
    }

    public String getAuthenMethod() {
        return this.authenMethod;
    }

    public EKyc authenMethod(String authenMethod) {
        this.authenMethod = authenMethod;
        return this;
    }

    public void setAuthenMethod(String authenMethod) {
        this.authenMethod = authenMethod;
    }

    public String getOtpReceiveMethod() {
        return this.otpReceiveMethod;
    }

    public EKyc otpReceiveMethod(String otpReceiveMethod) {
        this.otpReceiveMethod = otpReceiveMethod;
        return this;
    }

    public void setOtpReceiveMethod(String otpReceiveMethod) {
        this.otpReceiveMethod = otpReceiveMethod;
    }

    public Boolean getAdvancedCashIncluded() {
        return this.advancedCashIncluded;
    }

    public EKyc advancedCashIncluded(Boolean advancedCashIncluded) {
        this.advancedCashIncluded = advancedCashIncluded;
        return this;
    }

    public void setAdvancedCashIncluded(Boolean advancedCashIncluded) {
        this.advancedCashIncluded = advancedCashIncluded;
    }

    public String getSmsMethod() {
        return this.smsMethod;
    }

    public EKyc smsMethod(String smsMethod) {
        this.smsMethod = smsMethod;
        return this;
    }

    public void setSmsMethod(String smsMethod) {
        this.smsMethod = smsMethod;
    }

    public Boolean getEmailNotification() {
        return this.emailNotification;
    }

    public EKyc emailNotification(Boolean emailNotification) {
        this.emailNotification = emailNotification;
        return this;
    }

    public void setEmailNotification(Boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    public String getReferral() {
        return this.referral;
    }

    public EKyc referral(String referral) {
        this.referral = referral;
        return this;
    }

    public void setReferral(String referral) {
        this.referral = referral;
    }

    public String getPartnerId() {
        return this.partnerId;
    }

    public EKyc partnerId(String partnerId) {
        this.partnerId = partnerId;
        return this;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerName() {
        return this.partnerName;
    }

    public EKyc partnerName(String partnerName) {
        this.partnerName = partnerName;
        return this;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public Boolean getCustomerSupport() {
        return this.customerSupport;
    }

    public EKyc customerSupport(Boolean customerSupport) {
        this.customerSupport = customerSupport;
        return this;
    }

    public void setCustomerSupport(Boolean customerSupport) {
        this.customerSupport = customerSupport;
    }

    public String getCsPartnerId() {
        return this.csPartnerId;
    }

    public EKyc csPartnerId(String csPartnerId) {
        this.csPartnerId = csPartnerId;
        return this;
    }

    public void setCsPartnerId(String csPartnerId) {
        this.csPartnerId = csPartnerId;
    }

    public String getCsName() {
        return this.csName;
    }

    public EKyc csName(String csName) {
        this.csName = csName;
        return this;
    }

    public void setCsName(String csName) {
        this.csName = csName;
    }

    public String getContractId() {
        return this.contractId;
    }

    public EKyc contractId(String contractId) {
        this.contractId = contractId;
        return this;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getContractStatus() {
        return this.contractStatus;
    }

    public EKyc contractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
        return this;
    }

    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
    }

    public Boolean getFatca() {
        return this.fatca;
    }

    public EKyc fatca(Boolean fatca) {
        this.fatca = fatca;
        return this;
    }

    public void setFatca(Boolean fatca) {
        this.fatca = fatca;
    }

    public String getContractNo() {
        return this.contractNo;
    }

    public EKyc contractNo(String contractNo) {
        this.contractNo = contractNo;
        return this;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public EKyc accountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOcrLogId() {
        return this.ocrLogId;
    }

    public EKyc ocrLogId(String ocrLogId) {
        this.ocrLogId = ocrLogId;
        return this;
    }

    public void setOcrLogId(String ocrLogId) {
        this.ocrLogId = ocrLogId;
    }

    public String getCardLivenessLogId() {
        return this.cardLivenessLogId;
    }

    public EKyc cardLivenessLogId(String cardLivenessLogId) {
        this.cardLivenessLogId = cardLivenessLogId;
        return this;
    }

    public void setCardLivenessLogId(String cardLivenessLogId) {
        this.cardLivenessLogId = cardLivenessLogId;
    }

    public String getCardRearLogId() {
        return this.cardRearLogId;
    }

    public EKyc cardRearLogId(String cardRearLogId) {
        this.cardRearLogId = cardRearLogId;
        return this;
    }

    public void setCardRearLogId(String cardRearLogId) {
        this.cardRearLogId = cardRearLogId;
    }

    public String getCompareLogId() {
        return this.compareLogId;
    }

    public EKyc compareLogId(String compareLogId) {
        this.compareLogId = compareLogId;
        return this;
    }

    public void setCompareLogId(String compareLogId) {
        this.compareLogId = compareLogId;
    }

    public String getFaceLivenessLogId() {
        return this.faceLivenessLogId;
    }

    public EKyc faceLivenessLogId(String faceLivenessLogId) {
        this.faceLivenessLogId = faceLivenessLogId;
        return this;
    }

    public void setFaceLivenessLogId(String faceLivenessLogId) {
        this.faceLivenessLogId = faceLivenessLogId;
    }

    public String getFaceMaskLogId() {
        return this.faceMaskLogId;
    }

    public EKyc faceMaskLogId(String faceMaskLogId) {
        this.faceMaskLogId = faceMaskLogId;
        return this;
    }

    public void setFaceMaskLogId(String faceMaskLogId) {
        this.faceMaskLogId = faceMaskLogId;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EKyc)) {
            return false;
        }
        return id != null && id.equals(((EKyc) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EKyc{" +
            "id=" + getId() +
            ", identifierId='" + getIdentifierId() + "'" +
            ", fullName='" + getFullName() + "'" +
            ", phoneNo='" + getPhoneNo() + "'" +
            ", gender='" + getGender() + "'" +
            ", type='" + getType() + "'" +
            ", birthDay='" + getBirthDay() + "'" +
            ", expiredDate='" + getExpiredDate() + "'" +
            ", issueDate='" + getIssueDate() + "'" +
            ", issuePlace='" + getIssuePlace() + "'" +
            ", address='" + getAddress() + "'" +
            ", occupation='" + getOccupation() + "'" +
            ", homeTown='" + getHomeTown() + "'" +
            ", permanentProvince='" + getPermanentProvince() + "'" +
            ", permanentDistrict='" + getPermanentDistrict() + "'" +
            ", permanentAddress='" + getPermanentAddress() + "'" +
            ", contactProvince='" + getContactProvince() + "'" +
            ", contactDistrict='" + getContactDistrict() + "'" +
            ", contactAddress='" + getContactAddress() + "'" +
            ", email='" + getEmail() + "'" +
            ", referrerIdName='" + getReferrerIdName() + "'" +
            ", referrerBranch='" + getReferrerBranch() + "'" +
            ", bankAccount='" + getBankAccount() + "'" +
            ", accountName='" + getAccountName() + "'" +
            ", bankName='" + getBankName() + "'" +
            ", branch='" + getBranch() + "'" +
            ", nationality='" + getNationality() + "'" +
            ", status='" + getStatus() + "'" +
            ", frontImageUrl='" + getFrontImageUrl() + "'" +
            ", backImageUrl='" + getBackImageUrl() + "'" +
            ", portraitImageUrl='" + getPortraitImageUrl() + "'" +
            ", signatureImageUrl='" + getSignatureImageUrl() + "'" +
            ", tradingCodeImageUrl='" + getTradingCodeImageUrl() + "'" +
            ", isMargin='" + getIsMargin() + "'" +
            ", matchingRate=" + getMatchingRate() +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", branchId='" + getBranchId() + "'" +
            ", channelId='" + getChannelId() + "'" +
            ", eKycId='" + geteKycId() + "'" +
            ", taxNumber='" + getTaxNumber() + "'" +
            ", onlineTrading='" + getOnlineTrading() + "'" +
            ", authenMethod='" + getAuthenMethod() + "'" +
            ", otpReceiveMethod='" + getOtpReceiveMethod() + "'" +
            ", advancedCashIncluded='" + getAdvancedCashIncluded() + "'" +
            ", smsMethod='" + getSmsMethod() + "'" +
            ", emailNotification='" + getEmailNotification() + "'" +
            ", referral='" + getReferral() + "'" +
            ", partnerId='" + getPartnerId() + "'" +
            ", partnerName='" + getPartnerName() + "'" +
            ", customerSupport='" + getCustomerSupport() + "'" +
            ", csPartnerId='" + getCsPartnerId() + "'" +
            ", csName='" + getCsName() + "'" +
            ", contractId='" + getContractId() + "'" +
            ", contractStatus='" + getContractStatus() + "'" +
            ", fatca='" + getFatca() + "'" +
            ", contractNo='" + getContractNo() + "'" +
            ", accountNumber='" + getAccountNumber() + "'" +
            ", ocrLogId='" + getOcrLogId() + "'" +
            ", cardLivenessLogId='" + getCardLivenessLogId() + "'" +
            ", cardRearLogId='" + getCardRearLogId() + "'" +
            ", compareLogId='" + getCompareLogId() + "'" +
            ", faceLivenessLogId='" + getFaceLivenessLogId() + "'" +
            ", faceMaskLogId='" + getFaceMaskLogId() + "'" +
            "}";
    }
}
