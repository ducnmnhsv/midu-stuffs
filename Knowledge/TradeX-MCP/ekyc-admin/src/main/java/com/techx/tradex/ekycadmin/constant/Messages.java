package com.techx.tradex.ekycadmin.constant;

public interface Messages {
    String IDENTIFIER_IS_REQUIRED_WITH_GRANT_TYPE = "Request param 'identifierId' is is required for the case of grant_type is {0}";
    String E_KYC_ID_IS_REQUIRED_WITH_GRANT_TYPE = "Request param 'eKycId' is is required for the case of grant_type is {0}";
    String GRANT_TYPE_IS_INVALID = "grant_type = {0} is in valid";
    String TOKEN_IS_INVALID = "token is in valid: {0}";
    String TOKEN_IS_REQUIRED = "token is required";
    String FPT_LOGIN_IS_EMPTY = "FPT login response is empty";
    String FPT_TOKEN_IS_INVALID = "FPT login: access token is empty";
    String TOKEN_UD_NUMBER_IS_EMPTY = "jwt 'token.ud' object info is empty";
    String IDENTIFIER_NUMBER_IS_EMPTY = "jet 'token.ud.identifierNumber' is empty";
    String ENVELOPES_RECIPIENT_RESPONSE_IS_EMPTY = "API envelopesRecipient response is empty";
    String EKYC_NOT_FOUND = "EKYC_NOT_FOUND";
    String EKYC_ID_IS_REQUIRED = "EKYC_ID_IS_REQUIRED";
    String E_CONTRACT_INFO_NOT_FOUND = "E_CONTRACT_INFO_NOT_FOUND";
    String ACCOUNT_NUMBER_NOT_AVAILABLE = "ACCOUNT_NUMBER_NOT_AVAILABLE";
    String EKYC_ID_NOT_ACCESSIBLE_BY_THIS_SESSION = "Ekyc id is not accessible by this session";
}
