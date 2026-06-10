package com.techx.tradex.ekycadmin.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "system";
    public static final String DEFAULT_LANGUAGE = "vi";
    public static final String OBJECT_NOT_FOUND = "OBJECT_NOT_FOUND";
    public static final String KIS_E_KYC_OTP_ = "KIS_E_KYC_OTP_";
    public static final String KIS_E_KYC_OTP_KEY_ = "KIS_E_KYC_OTP_KEY_";
    public static final String INCORRECT_OTP = "INCORRECT_OTP";
    public static final String ID_NOT_FOUND = "ID_NOT_FOUND";
    public static final String OTP_GENERATE_TO_FAST = "OTP_GENERATE_TO_FAST";
    public static final String OTP_LIMIT_GENERATE = "OTP_LIMIT_GENERATE";
    public static final String OTP_WRONG_TYPE = "OTP_WRONG_TYPE";
    public static final String OTP_WRONG_USER = "OTP_WRONG_USER";
    public static final String INVALID_OTP_KEY = "INVALID_OTP_KEY";
    public static final Map<String, String> TOPIC = new HashMap() {
        {
            put("PHONE_NO", "SMS");
            put("EMAIL", "ONESIGNAL_EMAIL");
        }
    };
    public static final String FIELD_IS_REQUIRED = "FIELD_IS_REQUIRED";
    public static final String INVALID_VALUE = "INVALID_VALUE";
    public static String accepted = "accepted";
    public static final String E_KYC_ID_FIELD = "eKycId";

    private Constants() {}

    public static final String EKYC_ALREADY_EXISTED = "EKYC_ALREADY_EXISTED";
    public static final String EXISTED_WAITING_CONFIRMATION = "EXISTED_WAITING_CONFIRMATION";
    public static final String ACCOUNT_CREATED = "ACCOUNT_CREATED";

    public static final String E_KYC_ID_IS_NOT_BE_EMPTY = "E_KYC_ID_IS_NOT_BE_EMPTY";
    public static final String E_KYC_INFO_IS_NOT_FOUND = "EKyc info is not found with id = {0}";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String DATE_FORMAT_DMY = "dd/MM/yyyy";
    public static final String DATE_FORMAT_yyyyMMdd = "yyyyMMdd";
    public static final String MTS_MESSAGE = "[{0}] Message: {1}";
    public static final Integer DEFAULT_PAGE = 0;
    public static final Integer DEFAULT_SIZE = 100;
    public static final Boolean DEFAULT_CHECK_AUTHENTICATE = true;

    public static final String EKYC_UPDATE_CONTRACT_STATUS_ERROR = "EKYC_UPDATE_CONTRACT_STATUS_ERROR";

    public static final String EKYC_UPLOAD_IMAGE_ERROR = "EKYC_UPLOAD_IMAGE_ERROR";
    public static final String DATE_TIME_FORMAT_yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public static final String E_KYC_UPDATE_ACCOUNT_NUMBER_JOB = "E_KYC_ADMIN_update_account_number_job";
    public static final String INITIATE_FPT_E_CONTRACT_JOB = "E_KYC_ADMIN_initiate_fpt_e_contract_job";

    public static class EContract {

        public static final String PREFIX_ACC_NUM = "039C";
        public static final String GENDER_MALE = "Male";
        public static final String GENDER_FEMALE = "Female";
        public static final String DEFAULT_X_VAL = "x";
        public static final String ISSUE_PLACE_CCSQLHCVTTXH = "CỤC CẢNH SÁT QUẢN LÝ HÀNH CHÍNH VỀ TRẬT TỰ XÃ HỘI";
        public static final String ISSUE_PLACE_CCSDKQLCTVDLQGVDC = "CỤC CẢNH SÁT ĐKQL CƯ TRÚ VÀ ĐLQG VỀ DÂN CƯ";
        public static final String ACC_NUM_NOT_SUPPORTED = "Not supported account number = {0}";
        public static final String OTP = "otp";
        public static final String TOKEN = "token";
        public static final String ADVANCED = "advanced";
        public static final String BASIC = "basic";
        public static final String ONE = "1";
        public static final String TWO = "2";
        public static final String THREE = "3";
        public static final String FOUR = "4";
        public static final String LONG_TERM = "long_term";
        public static final String MID_TERM = "mid_term";
        public static final String SHORT_TERM = "short_term";
        public static final String LOW = "low";
        public static final String NORMAL = "normal";
        public static final String HIGH = "high";
        public static final String NULL_VALUE = "null";
        public static final String FPT = "FPT";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String PASSWORD = "password";
        public static final String PASSWORD_OTP = "password_otp";
        public static final String BIOMETRIC = "biometric";
        public static final String BIOMETRIC_OTP = "biometric_otp";
        public static final String REQUESTER = "requester";
        // Fields
        public static final String vEnvName = "envName";
        public static final String vEnvNo = "envNo";
        public static final String vEnvDate = "envDate";
        public static final String vEnvSubmittedFrom = "envSubmittedFrom";
        public static final String vP001 = "p_001";
        public static final String vP001R001 = "p_001_r_001";
        public static final String vP002 = "p_002";
        public static final String vP002R001 = "p_002_r_001";
        public static final String vlk1 = "lk1";
        public static final String vllk2 = "llk2";
        public static final String vlk3 = "lk3";
        public static final String vlk4 = "lk4";
        public static final String vlk5 = "lk5";
        public static final String vlk6 = "lk6";
        public static final String vtt1 = "tt1";
        public static final String vtt2 = "tt2";
        public static final String vtt3 = "tt3";
        public static final String vtt4 = "tt4";
        public static final String vtt5 = "tt5";
        public static final String vtt6 = "tt6";
        public static final String vkq1 = "kq1";
        public static final String vkq2 = "kq2";
        public static final String vkq3 = "kq3";
        public static final String vkq4 = "kq4";
        public static final String vkq5 = "kq5";
        public static final String vkq6 = "kq6";
        public static final String vdate = "date";
        public static final String vFullname = "Fullname";
        public static final String vNationlity = "Nationlity";
        public static final String vDateOfBirth = "Date_of_birth";
        public static final String vGenderMale = "Gender_male";
        public static final String vGenderFemale = "Gender_female";
        public static final String vIdNumber = "ID_number";
        public static final String vIssueDate = "Issue_date";
        public static final String vIssueOrganization = "Issue_organization";
        public static final String vHomeAddress = "Home_address";
        public static final String vContactAddress = "Contact_address";
        public static final String vPhoneNumber = "Phone_number";
        public static final String vEmail = "Email";
        public static final String vTaxCode = "Tax_code";
        public static final String vNhsvRepresentative = "NHSV_representative";
        public static final String vNhsvRepresentativePotition = "NHSV_representative_potition";
        public static final String vAuthorizationDocNo = "Authorization_doc_No";
        public static final String vAuthorizationDate = "Authorization_date";
        public static final String vAccountTypeA = "Account_Type_A";
        public static final String vAccountTypeB = "Account_Type_B";
        public static final String vAccountTypeC = "Account_Type_C";
        public static final String vAccountMarginYes = "Account_margin_Yes";
        public static final String vAccountMarginNo = "Account_margin_No";
        public static final String vTradeOnlineYes = "Trade_online_Yes";
        public static final String vTradeOnlineNo = "Trade_online_No";
        public static final String vAuthenticationOtp = "Authentication_OTP";
        public static final String vAuthenticationToken = "Authentication_token";
        public static final String vAdvanceMoneyYes = "Advance_money_Yes";
        public static final String vAdvanceMoneyNo = "Advance_money_No";
        public static final String vSmsReceiveYes = "SMS_receive_Yes";
        public static final String vSmsReceiveNo = "SMS_receive_No";
        public static final String vBeneficiary1 = "Beneficiary1";
        public static final String vBankAccount1 = "Bank_account1";
        public static final String vBankName1 = "Bank_name1";
        public static final String vBeneficiary2 = "Beneficiary2";
        public static final String vBankAccount2 = "Bank_account2";
        public static final String vBankName2 = "Bank_name2";
        public static final String vBeneficiary3 = "Beneficiary3";
        public static final String vBankAccount3 = "Bank_account3";
        public static final String vBankName3 = "Bank_name3";
        public static final String vBoFullname = "BO_fullname";
        public static final String vBoDateOfBirth = "BO_date_of_birth";
        public static final String vBoNationality = "BO_nationality";
        public static final String vBoIdNumber = "BO_ID_number";
        public static final String vBoIssueDate = "BO_issue_date";
        public static final String vBoIssueOrganization = "BO_issue_organization";
        public static final String vBoHomeAddress = "BO_home_address";
        public static final String vBoContractAddress = "BO_contract_address";
        public static final String vBoJob = "BO_job";
        public static final String vBoPosition = "BO_position";
        public static final String vBoPhoneNumber = "BO_phone_number";
        public static final String vBoVisaNumber = "BO_visa_number";
        public static final String vBoVisaIssueOrganization = "BO_visa_issue_organization";
        public static final String vBoForeignResidence = "BO_foreign_residence";
        public static final String vEmployee = "Employee";
        public static final String vAcquaintance = "Acquaintance";
        public static final String vAds = "Ads";
        public static final String vOthers = "Others";
        public static final String vYes = "Yes";
        public static final String vNo = "No";
        public static final String vInvestmentGoalLongTerm = "Investment_goal_Long_term";
        public static final String vInvestmentGoalMidTerm = "Investment_goal_Mid_term";
        public static final String vInvestmentGoalShortTerm = "Investment_goal_Short_term";
        public static final String vRiskLow = "Risk_low";
        public static final String vRiskNormal = "Risk_normal";
        public static final String vRiskHigh = "Risk_high";
        public static final String vExperienceYes = "Experience_Yes";
        public static final String vExperienceNo = "Experience_No";
        public static final String vInternalCompanyName1 = "Internal_company_name1";
        public static final String vInternalStock1 = "Internal_Stock1";
        public static final String vInternalPosition1 = "Internal_position1";
        public static final String vInternalCompanyName2 = "Internal_company_name2";
        public static final String vInternalStock2 = "Internal_Stock2";
        public static final String vInternalPosition2 = "Internal_position2";
        public static final String vOwnCompanyName1 = "Own_company_name1";
        public static final String vOwnStock1 = "Own_stock1";
        public static final String vOwnPosition1 = "Own_position1";
        public static final String vOwnCompanyName2 = "Own_company_name2";
        public static final String vOwnStock2 = "Own_stock2";
        public static final String vOwnPosition2 = "Own_position2";
        public static final String vFatcaA = "FATCA_A";
        public static final String vFatcaB = "FATCA_B";
        public static final String vFatcaC = "FATCA_C";
        public static final String vContractNumber = "Contract_number";
        public static final String vAcquaintanceName = "Acquaintance_name";
        public static final String vCaregiverName = "Caregiver_name";
        public static final String vEmployeeName = "Employee_name";
        public static final String vAdsName = "Ads_name";
        public static final String vOthersName = "Others_name";
        public static final String vDueDays = "dueDays";
        public static final String vRefId = "refId";
        public static final String vMailRecipient = "mail_recipient";
        public static final String vPhoneRecipient = "phone_recipient";
        public static final String vContactRecipient = "contact_recipient";
        public static final String vNameRecipient = "name_recipient";
        public static final String vApplicationFormRecipient = "applicationForm_recipient";
        public static final String envNameValuePattern = "HĐMTK-{0}-{1}";
        public static final String INVALID_DATA_MESSAGE = "INVALID_DATA_MESSAGE";
        public static final String DATA = "data";
        public static final String REF_ID = "refId";
        public static final String ENVELOP_ID = "envelopId";
        public static final String CONTRACT_STATUS = "contractStatus";
        public static final String CONTRACT_NOT_FOUND = "CONTRACT_NOT_FOUND";
        public static final String SIGN_FILE_CONTENT = "signFileContent";
        public static final String CONTRACT_FILE_CONTENT = "contractFileContent";
        public static final String CONTRACT_ID_ACTION = "contractIdAction";
        public static final String SAVE_CONTRACT_INFO_ERROR = "SAVE_CONTRACT_INFO_ERROR";
        public static final String SELECTOR = "flow_processing_nhsv_get_contract_info";

    }
    public static final String PHONENO_LOCK_INCORRECT_OTP_MAX = "PHONENO_LOCK_INCORRECT_OTP_MAX";
    public static final String INCORRECT_OTP_MAX = "INCORRECT_OTP_MAX";
    public static final int FRONT_IMG_KIND = 2;
    public static final int BACK_IMG_KIND = 3;

    public static final String EKYC_SESSION_ID_PREFIX = "EKYC_SESSION_ID_";
    public static final long EKYC_SESSION_ID_EXPIRE_TIME = 8 * 60 * 60 * 1000L;
}
