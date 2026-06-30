package com.techx.tradex.ekycadmin.models.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.techx.tradex.common.constants.ErrorCodeEnums;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.model.requests.DataRequest;
import com.techx.tradex.common.utils.DefaultUtils;
import com.techx.tradex.common.utils.StringUtils;
import com.techx.tradex.common.utils.validator.*;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.domain.enumeration.EkycType;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import com.techx.tradex.ekycadmin.models.deserializer.*;
import com.techx.tradex.ekycadmin.models.enums.LotteEKycAuthenMethod;
import com.techx.tradex.ekycadmin.models.enums.LotteEKycGrpType;
import com.techx.tradex.ekycadmin.models.enums.LotteEKycOtpReceiveMethod;
import com.techx.tradex.ekycadmin.models.enums.LotteEKycSmsMethod;
import com.techx.tradex.ekycadmin.utils.Util;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Data;

/**
 * example:
 * {
 * "matchingRate": 96.916,
 * "identifierId": "001099009118",
 * "fullName": "NGUYỄN THÀNH AN",
 * "birthDay": "19990612",
 * "issueDate": "20190524",
 * "issuePlace": "CỤC TRƯỞNG CỤC CẢNH SÁT  QUẢN LÝ HÀNH CHÍNH VỀ TRẬT TỰ XÃ HỘI",
 * "address": "Thôn Đoài,Kim Nỗ, Đông Anh, Hà Nội",
 * "type": "CC",
 * "backImageUrl": "https://trading.kisvn.vn:9001/tradex-vn/ekyc_back_image_001099009118",
 * "frontImageUrl": "https://trading.kisvn.vn:9001/tradex-vn/ekyc_front_image_001099009118",
 * "portraitImageUrl": "https://trading.kisvn.vn:9001/tradex-vn/ekyc_portrait_image_001099009118",
 * "phoneNo": "0352612699",
 * "gender": "Male",
 * "occupation": "",
 * "permanentProvince": " Hà Nội",
 * "permanentDistrict": " Đông Anh",
 * "permanentAddress": "Thôn Đoài,Kim Nỗ",
 * "contactProvince": "Thành phố Hà Nội",
 * "contactDistrict": "Quận Ba Đình",
 * "contactAddress": "Doai-Kim No-Dong Anh",
 * "email": "anchuyensupham@gmail.com",
 * "referrerIdName": "",
 * "referrerBranch": "HEAD OFFICE",
 * "bankAccount": "00000",
 * "accountName": "test",
 * "bankName": "NHTMCP DTVAPT VN",
 * "branch": "CN SA DEC ",
 * "signatureImageUrl": "https://trading.kisvn.vn:9001/tradex-vn/ekyc_signature_image_001099009118"
 * }
 */

@Data
@JsonDeserialize(using = EKycAddReqDeserializer.class)
public class EKycAddReq extends DataRequest {

    private String identifierId;
    private String fullName;
    private String phoneNo;
    private String gender;
    private EkycType type;
    private String birthDay;
    private String expiredDate;
    private String expireDate;
    private String issueDate;
    private String issuePlace;
    private String issuePlaceCode;
    private String address;
    private String frontImageUrl;
    private String backImageUrl;
    private String portraitImageUrl;
    private String signatureImageUrl;
    private String tradingCodeImageUrl;
    private Boolean isMargin = false;
    private double matchingRate;
    private String occupation;
    private String homeTown;
    private String permanentProvince;
    private String permanentDistrict;
    private String permanentAddress;
    private String contactProvince;
    private String contactDistrict;
    private String contactAddress;
    private String email;
    private String referrerIdName;
    private String referrerBranch;
    private String bankAccount;
    private String accountName;
    private String bankName;
    private String branch;
    private String branchId;
    private String nationality = "Việt Nam";
    private String logId;
    private String rawData;
    private String channelId;

    // lotte-nhsv
    private String eKycId;
    private String groupType;
    private Boolean marginInclued; // false ----> core: 1 true ------> core: 2
    private Boolean derivativesIncluded; // false ----> acnt_dr_tp: 1 true ----> acnt_dr_tp: 2
    private String job; // job detail, transmitted as job_detail to Lotte and FPT
    private Boolean onlineTrading; // false ----> core: N true -----> core: Y
    private String authenMethod; // otp ----> core: 1 token -----> core: 2
    private String otpReceiveMethod; // email ------> core: Y express -----> core: N
    private Boolean advancedCashIncluded; // false ----> core: N true -----> core: Y
    private String smsMethod; // basic ------> core: 1 advanced ------> core: 2
    private Boolean emailNotification; // false ----> core: N true -----> core: Y
    private List<BankList> bankList;
    private String referral; // 1: Nhân viên/CTV, 2: Khách hàng, 3: Quảng cáo, 4: Khác (disable nếu có partner)
    private String partnerId; // Id của partner - ctvtimo: Timo, ctvvpb: VPB, woori1: Worri 1, woori2: Worri 2, accesstrade: Accesstrade
    private Boolean customerSupport; // false ----> core: N true -----> core: Y
    private String csPartnerId; // Id của partner - ctvtimo: Timo, ctvvpb: VPB, woori1: Worri 1, woori2: Worri 2, accesstrade: Accesstrade
    private Boolean fatca;
    private String taxNo;
    private String csName;
    private String partnerName;
    private BeneficiaryOwner beneficiaryOwner;
    private InvestmentExperience investmentExperience;
    private String deviceUniqueId;
    private String dataSign;
    private String dataBase64;
    private String ocrLogId;
    private String cardLivenessLogId;
    private String cardRearLogId;
    private String compareLogId;
    private String faceLivenessLogId;
    private String faceMaskLogId;

    @Data
    public static class BankList {

        private String bankId;
        private String bankName;
        private String bankAccNo;
        private String ownerName;
        private String branchId;
    }

    @Data
    public static class BeneficiaryOwner {

        private String fullName;
        private String birthDay;
        private String nationality;
        private String identifierId;
        private String issueDate;
        private String issuePlace;
        private String permanentAddress;
        private String contactAddress;
        private String occupation;
        private String position;
        private String phoneNumber;
        private String visaNo;
        private String visaIssuePlace;
        private String foreignResidence;
    }

    @Data
    public static class InvestmentExperience {

        private String investmentGoal;
        private String risk;
        private Boolean experienced;
        private List<PublicCoop> publicCoop;
        private List<Blockholder> blockholder;
    }

    @Data
    public static class PublicCoop {

        private String companyName;
        private String stock;
        private String position;
    }

    @Data
    public static class Blockholder {

        private String companyName;
        private String stock;
        private String position;
    }

    public EKyc toEKyc(Integer idCardExpiredTime) throws ParseException {
        EKyc eKyc = new EKyc();
        eKyc.setIdentifierId(this.getIdentifierId());
        eKyc.setFullName(this.getFullName());
        eKyc.setPhoneNo(this.getPhoneNo());
        eKyc.setGender(this.getGender());
        if (this.getType() == null) { //request for lotte core no require field type
            eKyc.setType(EkycType.CC);
        } else {
            eKyc.setType(this.getType());
        }
        Date birthDay = DefaultUtils.DATE_FORMAT().parse(this.getBirthDay());
        Date issueDate = DefaultUtils.DATE_FORMAT().parse(this.getIssueDate());
        Date expiredDate = this.getExpiredDate() != null && !this.getExpiredDate().trim().equals("")
            ? DefaultUtils.DATE_FORMAT().parse(this.getExpiredDate())
            : Util.addYear(issueDate, idCardExpiredTime);

        eKyc.setBirthDay(dateToLocalDate(birthDay));
        eKyc.setExpiredDate(dateToLocalDate(expiredDate));
        eKyc.setIssueDate(dateToLocalDate(issueDate));
        if (this.getAddress() != null) {
            eKyc.setAddress(this.getAddress());
        } else {
            eKyc.setAddress(this.getPermanentAddress());
        }
        eKyc.setIssuePlace(this.getIssuePlace());
        eKyc.setOccupation(this.getOccupation());
        eKyc.setHomeTown(this.getHomeTown());
        eKyc.setPermanentProvince(this.getPermanentProvince());
        eKyc.setPermanentDistrict(this.getPermanentDistrict());
        eKyc.setPermanentAddress(this.getPermanentAddress());
        eKyc.setContactProvince(this.getContactProvince());
        eKyc.setContactDistrict(this.getContactDistrict());
        eKyc.setContactAddress(this.getContactAddress());
        eKyc.setEmail(this.getEmail());
        eKyc.setReferrerIdName(this.getReferrerIdName());
        eKyc.setReferrerBranch(this.getReferrerBranch());
        eKyc.setBankAccount(this.getBankAccount());
        eKyc.setAccountName(this.getAccountName());
        eKyc.setBankName(this.getBankName());
        eKyc.setBranch(this.getBranch());
        eKyc.setFrontImageUrl(this.getFrontImageUrl());
        eKyc.setBackImageUrl(this.getBackImageUrl());
        eKyc.setPortraitImageUrl(this.getPortraitImageUrl());
        eKyc.setSignatureImageUrl(this.getSignatureImageUrl());
        eKyc.setTradingCodeImageUrl(this.getTradingCodeImageUrl());
        eKyc.setGender(this.getGender());
        if (this.getMarginInclued() != null) {
            eKyc.setIsMargin(this.getMarginInclued());
        } else {
            eKyc.setIsMargin(this.isMargin);
        }
        eKyc.setStatus(Status.PENDING);
        eKyc.setMatchingRate(this.getMatchingRate());
        eKyc.setNationality(this.getNationality());
        eKyc.setCreatedAt(ZonedDateTime.now());
        eKyc.setUpdatedAt(ZonedDateTime.now());
        eKyc.setBranchId(branchId);
        eKyc.setChannelId(this.getChannelId());
        eKyc.seteKycId(this.getEKycId());
        if (StringUtils.isNotEmpty(this.getTaxNo())) {
            eKyc.setTaxNumber(this.getTaxNo());
        } else {
            eKyc.setTaxNumber(this.getIdentifierId());
        }
        eKyc.setOnlineTrading(this.getOnlineTrading());
        eKyc.setAuthenMethod(this.getAuthenMethod());
        eKyc.setOtpReceiveMethod(this.getOtpReceiveMethod());
        eKyc.setAdvancedCashIncluded(this.getAdvancedCashIncluded());
        eKyc.setSmsMethod(this.getSmsMethod());
        eKyc.setEmailNotification(this.getEmailNotification());
        eKyc.setReferral(this.getReferral());
        eKyc.setPartnerId(this.getPartnerId());
        eKyc.setPartnerName(this.getPartnerName());
        eKyc.setCustomerSupport(this.getCustomerSupport());
        eKyc.setCsPartnerId(this.getCsPartnerId());
        eKyc.setCsName(this.getCsName());
        eKyc.setFatca(this.getFatca());
        eKyc.setDerivativesIncluded(this.getDerivativesIncluded());
        eKyc.setJob(this.getJob());
        if (StringUtils.isNotEmpty(this.getOcrLogId())) {
            eKyc.setOcrLogId(this.getOcrLogId());
        }
        if (StringUtils.isNotEmpty(this.getCardLivenessLogId())) {
            eKyc.setCardLivenessLogId(this.getCardLivenessLogId());
        }
        if (StringUtils.isNotEmpty(this.getCardRearLogId())) {
            eKyc.setCardRearLogId(this.getCardRearLogId());
        }
        if (StringUtils.isNotEmpty(this.getCompareLogId())) {
            eKyc.setCompareLogId(this.getCompareLogId());
        }
        if (StringUtils.isNotEmpty(this.getFaceLivenessLogId())) {
            eKyc.setFaceLivenessLogId(this.getFaceLivenessLogId());
        }
        if (StringUtils.isNotEmpty(this.getFaceMaskLogId())) {
            eKyc.setFaceMaskLogId(this.getFaceMaskLogId());
        }
        return eKyc;
    }

    private LocalDate dateToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void validReqCreateEKycLotte() {
        new CombineValidator()
            .add(new StringValidator("email", this.email).empty())
            .add(new EnumValidator("groupType", this.groupType, LotteEKycGrpType.class).validate())
            .add(new StringValidator("phoneNo", this.phoneNo).empty())
            .check();
    }

    public void validReqAddEKycLotte(Integer maxBankList, Integer maxPublicCoop, Integer maxBlockholder) {
        CombineValidator combineValidator = new CombineValidator();
        combineValidator
            .add(new StringValidator("eKycId", this.eKycId).empty())
            .add(new StringValidator("fullName", this.fullName).empty())
            .add(new StringValidator("identifierId", this.identifierId).empty())
            .add(new StringValidator("birthDay", this.birthDay).empty())
            .add(new StringValidator("gender", this.gender).empty())
            .add(new StringValidator("issueDate", this.issueDate).empty())
            .add(new StringValidator("issuePlace", this.issuePlace).empty())
            .add(new StringValidator("permanentAddress", this.permanentAddress).empty())
            .add(new StringValidator("contactAddress", this.contactAddress).empty())
            .add(new StringValidator("branch", this.branch).empty())
            .add(new BoolenValidator("marginInclued", this.marginInclued).notNull())
            .add(new BoolenValidator("onlineTrading", this.onlineTrading).notNull())
            .add(new EnumValidator("authenMethod", this.authenMethod, LotteEKycAuthenMethod.class).validate())
            .add(new EnumValidator("otpReceiveMethod", this.otpReceiveMethod, LotteEKycOtpReceiveMethod.class).validate())
            .add(new BoolenValidator("advancedCashIncluded", this.advancedCashIncluded).notNull())
            .add(new EnumValidator("smsMethod", this.smsMethod, LotteEKycSmsMethod.class).validate())
            .add(new BoolenValidator("emailNotification", this.emailNotification).notNull())
            .add(new ListValidator("bankList", this.bankList).empty())
            .add(new StringValidator("referral", this.referral).empty())
            .add(new StringValidator("partnerId", this.partnerId).empty())
            .add(new BoolenValidator("customerSupport", this.customerSupport).notNull())
            .add(new BoolenValidator("fatca", this.fatca).notNull())
            .add(new NumberValidator("matchingRate", this.matchingRate).notEmpty())
            .add(new StringValidator("dataSign", this.dataSign).empty())
            .add(new StringValidator("dataBase64", this.dataBase64).empty())
            .add(new StringValidator("phoneNo", this.phoneNo).empty())
            .add(new StringValidator("email", this.email).empty());
        if (this.customerSupport != null && this.customerSupport) {
            combineValidator.add(new StringValidator("csPartnerId", this.csPartnerId).empty());
            combineValidator.add(new StringValidator("csName", this.csName).empty());
        }
        if (this.bankList != null) {
            if (this.bankList.size() > maxBankList) {
                throw new GeneralException(String.format("MAX_BANKS_%d", maxBankList));
            }
            IntStream
                .range(0, this.bankList.size())
                .forEach(
                    index -> {
                        BankList bank = this.bankList.get(index);
                        combineValidator
                            .add(new StringValidator(String.format("bankList[%d].bankId", index), bank.getBankId()).empty())
                            .add(new StringValidator(String.format("bankList[%d].bankName", index), bank.getBankName()).empty())
                            .add(new StringValidator(String.format("bankList[%d].bankAccNo", index), bank.getBankAccNo()).empty())
                            .add(new StringValidator(String.format("bankList[%d].ownerName", index), bank.getOwnerName()).empty())
                            .add(new StringValidator(String.format("bankList[%d].branchId", index), bank.getBranchId()).empty());
                    }
                );
        }
        if (this.beneficiaryOwner != null) {
            combineValidator
                .add(new StringValidator("beneficiaryOwner.fullName", this.beneficiaryOwner.fullName).empty())
                .add(new StringValidator("beneficiaryOwner.birthDay", this.beneficiaryOwner.birthDay).empty())
                .add(new StringValidator("beneficiaryOwner.nationality", this.beneficiaryOwner.nationality).empty())
                .add(new StringValidator("beneficiaryOwner.identifierId", this.beneficiaryOwner.identifierId).empty())
                .add(new StringValidator("beneficiaryOwner.issueDate", this.beneficiaryOwner.issueDate).empty())
                .add(new StringValidator("beneficiaryOwner.issuePlace", this.beneficiaryOwner.issuePlace).empty())
                .add(new StringValidator("beneficiaryOwner.permanentAddress", this.beneficiaryOwner.permanentAddress).empty())
                .add(new StringValidator("beneficiaryOwner.contactAddress", this.beneficiaryOwner.contactAddress).empty())
                .add(new StringValidator("beneficiaryOwner.occupation", this.beneficiaryOwner.occupation).empty())
                .add(new StringValidator("beneficiaryOwner.position", this.beneficiaryOwner.position).empty())
                .add(new StringValidator("beneficiaryOwner.phoneNumber", this.beneficiaryOwner.phoneNumber).empty())
                .add(new StringValidator("beneficiaryOwner.visaNo", this.beneficiaryOwner.visaNo).empty())
                .add(new StringValidator("beneficiaryOwner.visaIssuePlace", this.beneficiaryOwner.visaIssuePlace).empty());
        }
        if (this.investmentExperience != null) {
            if (this.investmentExperience.publicCoop != null) {
                if (this.investmentExperience.publicCoop.size() > maxPublicCoop) {
                    throw new GeneralException(String.format("PUBLIC_COOP_MAX_%d", maxPublicCoop));
                }
                IntStream
                    .range(0, this.investmentExperience.publicCoop.size())
                    .forEach(
                        index -> {
                            PublicCoop publicCoop = this.investmentExperience.publicCoop.get(index);
                            combineValidator
                                .add(
                                    new StringValidator(String.format("publicCoop[%d].companyName", index), publicCoop.getCompanyName())
                                        .empty()
                                )
                                .add(new StringValidator(String.format("publicCoop[%d].stock", index), publicCoop.getStock()).empty())
                                .add(
                                    new StringValidator(String.format("publicCoop[%d].position", index), publicCoop.getPosition()).empty()
                                );
                        }
                    );
            }
            if (this.investmentExperience.blockholder != null) {
                if (this.investmentExperience.blockholder.size() > maxBlockholder) {
                    throw new GeneralException(String.format("BLOCKHOLDER_MAX_%d", maxBlockholder));
                }
                IntStream
                    .range(0, this.investmentExperience.blockholder.size())
                    .forEach(
                        index -> {
                            Blockholder blockholder = this.investmentExperience.blockholder.get(index);
                            combineValidator
                                .add(
                                    new StringValidator(String.format("blockholder[%d].companyName", index), blockholder.getCompanyName())
                                        .empty()
                                )
                                .add(new StringValidator(String.format("blockholder[%d].stock", index), blockholder.getStock()).empty())
                                .add(
                                    new StringValidator(String.format("blockholder[%d].position", index), blockholder.getPosition()).empty()
                                );
                        }
                    );
            }
        }
        combineValidator.check();
    }

    public class BoolenValidator extends Validator<Boolean> {

        public BoolenValidator(String fieldName, Boolean fieldValue) {
            super(fieldName, fieldValue);
        }

        private boolean notNull = true;

        public BoolenValidator notNull() {
            this.notNull = true;
            return this;
        }

        @Override
        protected Object doCheck() {
            Object result = null;
            if (this.notNull && this.fieldValue == null) {
                this.addError(ErrorCodeEnums.EMPTY_VALUE.name(), this.fieldName);
                return null;
            }
            return result;
        }
    }
}
