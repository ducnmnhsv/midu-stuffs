package com.techx.tradex.ekycadmin.config;

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

    private Constants() {
    }

    public static final String EKYC_ALREADY_EXISTED = "EKYC_ALREADY_EXISTED";

    public static final String IDENTIFIER_ID_IS_NOT_BE_EMPTY = "The param identifier_id is not allowed to be empty.";
    public static final String EKYC_INFO_IS_NOT_FOUND = "Ekyc info is not found with identifier_id = {0}";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String DATE_FORMAT_DMY = "dd/MM/yyyy";
    public static class EContract {

        public static final String PREFIX_ACC_NUM = "039C";
        public static final String GENDER_MALE = "Male";
        public static final String GENDER_FEMALE = "Female";
        public static final String DEFAULT_X_VAL = "x";
        public static final String ISSUE_PLACE_CTCCSQLHCVTTXH = "CỤC TRƯỜNG CỤC CẢNH SÁT QUẢN LÝ HÀNH CHÍNH VỀ TRẬT TỰ XÃ HỘI";
        public static final String ISSUE_PLACE_CTCCSDKQLCTVDLQGVDC = "CỤC TRƯỞNG CỤC CẢNH SÁT ĐKQL CƯ TRÚ VÀ ĐLQG VỀ DÂN CƯ";
        public static final String ACC_NUM_NOT_SUPPORTED = "Not supported account number = {0}";
        public static final String OTP = "OTP";
        public static final String TOKEN = "TOKEN";
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
        // Fields
        public static final String vEnvName = "envName";
        public static final String vEnvNo = "envNo";
        public static final String vEnvDate = "envDate";
        public static final String vEnvSubmittedFrom = "envSubmittedFrom";
        public static final String vP001 = "p_001";
        public static final String vP001R001 = "p_001_r_001";
        public static final String vP002 = "p_002";
        public static final String vP002R001 = "p_002_r_001";
        public static final String v1e9c356aa77b310714b = "1e9c356aa77b310714b";
        public static final String v92401a712aac195309d = "92401a712aac195309d";
        public static final String vE2a61cdaff16252bf2c = "e2a61cdaff16252bf2c";
        public static final String v33a1bb78d564fb51549 = "33a1bb78d564fb51549";
        public static final String v03d8ca9fb78f932ac4c = "03d8ca9fb78f932ac4c";
        public static final String v107bda43770c7f0868e = "107bda43770c7f0868e";
        public static final String v8bae03609e2adf593e8 = "8bae03609e2adf593e8";
        public static final String vBb2549f3c0a46fb97f1 = "bb2549f3c0a46fb97f1";
        public static final String v1069134de49111ca169 = "1069134de49111ca169";
        public static final String v68a6ec9188c35c098eb = "68a6ec9188c35c098eb";
        public static final String v0ee357b68edf1711a86 = "0ee357b68edf1711a86";
        public static final String v283fef275acab6b03ae = "283fef275acab6b03ae";
        public static final String vAcb784056d934c21545 = "acb784056d934c21545";
        public static final String v32aea485433791721ed = "32aea485433791721ed";
        public static final String v2cc1100d3680872eccf = "2cc1100d3680872eccf";
        public static final String v1643ffe8cc931da1fcf = "1643ffe8cc931da1fcf";
        public static final String v995b61a4c40010cb18a = "995b61a4c40010cb18a";
        public static final String vF8172e0464ed15d1ec2 = "f8172e0464ed15d1ec2";
        public static final String v270e16df0ad4533c2ee = "270e16df0ad4533c2ee";
        public static final String v4081198ebc35a2e03ba = "4081198ebc35a2e03ba";
        public static final String v0702901a7acf0949824 = "0702901a7acf0949824";
        public static final String v77fb0d7a2efeb7aa54a = "77fb0d7a2efeb7aa54a";
        public static final String vCcdb868f6e42f210836 = "ccdb868f6e42f210836";
        public static final String v7e0bdeb7c686c530229 = "7e0bdeb7c686c530229";
        public static final String v3d0a0c356080eea1465 = "3d0a0c356080eea1465";
        public static final String vA0a5e2c221ea51e3cc3 = "a0a5e2c221ea51e3cc3";
        public static final String v19eaa6ce9145de0bbef = "19eaa6ce9145de0bbef";
        public static final String v1c82870a7f328982238 = "1c82870a7f328982238";
        public static final String v83a27cdb9b72e00fd1b = "83a27cdb9b72e00fd1b";
        public static final String v4524e6e7e0f75f0b684 = "4524e6e7e0f75f0b684";
        public static final String vAe6a3c678d831b57c0f = "ae6a3c678d831b57c0f";
        public static final String v9192369eb3b64253834 = "9192369eb3b64253834";
        public static final String v442a73f9fdd5cca7c8e = "442a73f9fdd5cca7c8e";
        public static final String vDe48a0618df665fde62 = "de48a0618df665fde62";
        public static final String vEf9ccd3905c2d83344c = "ef9ccd3905c2d83344c";
        public static final String vFbde6d3159b7086e897 = "fbde6d3159b7086e897";
        public static final String v3bbe85ca9b6d16cb270 = "3bbe85ca9b6d16cb270";
        public static final String v1e48b4927228f8466e3 = "1e48b4927228f8466e3";
        public static final String vE043e08aa48c869d8b3 = "e043e08aa48c869d8b3";
        public static final String v40b6eb8db278117e3fa = "40b6eb8db278117e3fa";
        public static final String vD02661a8998410bc731 = "d02661a8998410bc731";
        public static final String v59ae5bb9141f01d54dd = "59ae5bb9141f01d54dd";
        public static final String v8fd23ff078fe155ed09 = "8fd23ff078fe155ed09";
        public static final String v602dff5a941cc7b13b4 = "602dff5a941cc7b13b4";
        public static final String v71fcf42793657e51c88 = "71fcf42793657e51c88";
        public static final String v9540f2471a92f2d1bd8 = "9540f2471a92f2d1bd8";
        public static final String vDb4f60ddc025321ede2 = "db4f60ddc025321ede2";
        public static final String v1509486b2eda3620544 = "1509486b2eda3620544";
        public static final String v7f1da38761397c7f6a2 = "7f1da38761397c7f6a2";
        public static final String vD4547d5703b62f919df = "d4547d5703b62f919df";
        public static final String vCc896221da7c39ebb8d = "cc896221da7c39ebb8d";
        public static final String vC8bd20595a5323a5dbd = "c8bd20595a5323a5dbd";
        public static final String vD39902a1da0299d7cc3 = "d39902a1da0299d7cc3";
        public static final String vC62512caa17c99f83a1 = "c62512caa17c99f83a1";
        public static final String v70b7557915e44caa6d2 = "70b7557915e44caa6d2";
        public static final String v38bc696a3ac38440888 = "38bc696a3ac38440888";
        public static final String v792f431aa1a623a68e8 = "792f431aa1a623a68e8";
        public static final String v765fdc18ccd7d611fb5 = "765fdc18ccd7d611fb5";
        public static final String v66756949b2287d3717c = "66756949b2287d3717c";
        public static final String v779acd3287ac001968f = "779acd3287ac001968f";
        public static final String v1bcc048b00e731a2a9b = "1bcc048b00e731a2a9b";
        public static final String v1b1befff8d987a518c0 = "1b1befff8d987a518c0";
        public static final String v01e9ebf6375e55275d3 = "01e9ebf6375e55275d3";
        public static final String vFc669f211b011e9c19d = "fc669f211b011e9c19d";
        public static final String vA3d47d3769bd3a5a540 = "a3d47d3769bd3a5a540";
        public static final String v738917e9fa2edd4ef63 = "738917e9fa2edd4ef63";
        public static final String v4441699be366a0965b5 = "4441699be366a0965b5";
        public static final String vE744ff755d51c1e6f91 = "e744ff755d51c1e6f91";
        public static final String vD7bd2c6b38ef7e4884e = "d7bd2c6b38ef7e4884e";
        public static final String v569279119fbab7eed50 = "569279119fbab7eed50";
        public static final String v7ce91e2079edd6f99c6 = "7ce91e2079edd6f99c6";
        public static final String vAa9319ac600f09fac7a = "aa9319ac600f09fac7a";
        public static final String v9b2a2dbc5a9377399ce = "9b2a2dbc5a9377399ce";
        public static final String vEfd5cdaf6c9b4575423 = "efd5cdaf6c9b4575423";
        public static final String v0d51872a83b89556eb9 = "0d51872a83b89556eb9";
        public static final String v9af5bb9f8e3fce89c54 = "9af5bb9f8e3fce89c54";
        public static final String vD93565e35cf1cdb758d = "d93565e35cf1cdb758d";
        public static final String v167b75982197c8981fd = "167b75982197c8981fd";
        public static final String vF803914f3dc89187c28 = "f803914f3dc89187c28";
        public static final String vB5ea99a27d38bcc80fe = "b5ea99a27d38bcc80fe";
        public static final String vC110c72cdfcc93fd8d5 = "c110c72cdfcc93fd8d5";
        public static final String v97e75e4f42351d1f5ba = "97e75e4f42351d1f5ba";
        public static final String vBe6a5800f79e589f063 = "be6a5800f79e589f063";
        public static final String v2e9d78a6ad2369bb2e4 = "2e9d78a6ad2369bb2e4";
        public static final String vD664100e8063e5538f0 = "d664100e8063e5538f0";
        public static final String v394f698d080657259af = "394f698d080657259af";
        public static final String v27fa6864b552063c970 = "27fa6864b552063c970";
        public static final String vF9cbf49ddb5af02a0fd = "f9cbf49ddb5af02a0fd";
        public static final String v2654b5ac4f7464491bb = "2654b5ac4f7464491bb";
        public static final String vB14a5c27515582a7f9f = "b14a5c27515582a7f9f";
        public static final String v68f42ee4cf5b5802400 = "68f42ee4cf5b5802400";
        public static final String vFee497b1c506f3bd960 = "fee497b1c506f3bd960";
        public static final String v201328f7b618965d663 = "201328f7b618965d663";
        public static final String vA817a966a43a3a7fa9f = "a817a966a43a3a7fa9f";
        public static final String v53cc45ab77ddead0751 = "53cc45ab77ddead0751";
        public static final String v815bac24f27e5ffff0f = "815bac24f27e5ffff0f";
        public static final String v577bc6171c641cdfb5d = "577bc6171c641cdfb5d";
        public static final String vD5e1298a7711cfd8be0 = "d5e1298a7711cfd8be0";
        public static final String vFe17e338fdf42eae7bd = "fe17e338fdf42eae7bd";
        public static final String v0b3081d9747e80e501e = "0b3081d9747e80e501e";
        public static final String v77e5cded8066d41c983 = "77e5cded8066d41c983";
        public static final String vB0791be389c9efc5300 = "b0791be389c9efc5300";
        public static final String v8866e6c8f13792d0607 = "8866e6c8f13792d0607";
        public static final String v584b7d2240b1b0c8bde = "584b7d2240b1b0c8bde";
        public static final String v44a9fb73194be929842 = "44a9fb73194be929842";
        public static final String v078ae99018086c95f5c = "078ae99018086c95f5c";
        public static final String v93c5e31e58222d90dfd = "93c5e31e58222d90dfd";
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
    }
    public static final String EXISTED_WAITING_CONFIRMATION = "EXISTED_WAITING_CONFIRMATION";
    public static final String ACCOUNT_CREATED = "ACCOUNT_CREATED";

    public static final String EKYC_UPDATE_CONTRACT_STATUS_ERROR = "EKYC_UPDATE_CONTRACT_STATUS_ERROR";

    public static final String EKYC_UPLOAD_IMAGE_ERROR = "EKYC_UPLOAD_IMAGE_ERROR";
}
