package com.difisoft.nhsv.admin.constant;

import java.math.BigDecimal;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "system";
    public static final String DEFAULT_LANGUAGE = "en";

    public static final String INVALID_EVENT_CODE = "INVALID_EVENT_CODE";

    public static final String INVALID_TYPE = "INVALID_TYPE";

    public static final String INVALID_NOTE = "INVALID_NOTE";

    public static final String INVALID_RATIO = "INVALID_RATIO";

    public static final String INVALID_EFFECTIVE_DATE = "INVALID_EFFECTIVE_DATE";

    public static final String INVALID_PRICE = "INVALID_PRICE";

    public static final String EVENT_ADD_SUCCESS = "EVENT_ADD_SUCCESS";

    public static final String INVALID_DISCLOSURE_DATE = "INVALID_DISCLOSURE_DATE";

    public static final String EVENT_DOES_NOT_EXIST = "EVENT_DOES_NOT_EXIST";

    public static final String EVENT_UPDATE_SUCCESS = "EVENT_UPDATE_SUCCESS";

    public static final String EVENT_LIST_CANT_NOT_BE_EMPTY = "EVENT_LIST_CANT_NOT_BE_EMPTY";

    public static final String EVENT_DELETED_SUCCESS = "EVENT_DELETED_SUCCESS";

    public static final String FROM_EFFECTIVE_DATE_MUST_LESS_THAN_TO_EFFECTVIE_DATE =
        "FROM_EFFECTIVE_DATE_MUST_LESS_THAN_TO_EFFECTVIE_DATE";

    public static final String CODE_MUST_NOT_NULL = "CODE_MUST_NOT_NULL";

    public static final String INVALID_BASIC_PRICE = "INVALID_BASIC_PRICE";

    public static final String INVALID_TOTAL_ADJUST_RATE = "INVALID_TOTAL_ADJUST_RATE";

    public static final int DEFAULT_OFFSET = 0;
    public static final int PAGE_SIZE_1 = 1;

    public static final String UNSORTED = "UNSORTED";

    public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";

    public static final String CHAT_ROOM_NOT_FOUND = "CHAT_ROOM_NOT_FOUND";
    public static final String DATE_FORMAT_yyyyMMdd = "yyyyMMdd";
    public static final String DATE_FORMAT_ddMMyyyy_hhmmss = "dd/MM/yyyy hh:mm:ss";

    public static final String DATE_FORMAT_yyyy_MM_dd = "yyyy-MM-dd";
    public static final String DATE_FORMAT_dd_MM_yyyy_HH_mm_ss = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FORMAT_yyyyMMdd_hh_mm_ss = "yyyyMMdd hh:mm:ss";
    public static final String DATE_FORMAT_hhmmss_ddMMYYYY = "hh:mm:ss dd/mm/yyyy";
    public static final String DATE_FORMAT_yyyy_MM_dd_hh_mm_ss = "yyyy_MM_dd_hh_mm_ss";
    public static final String DATE_FORMAT_yyyy_MM_dd_HH_mm_ss_SSSSSS = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    public static final String DATE_FORMAT_PATTERN_1 = "dd/MM/yyyy";

    public static final String CHAT_ROOM_OWNER_IS_DEACTIVATED = "CHAT_ROOM_OWNER_IS_DEACTIVATED";
    public static final String GROUP_NAME_EXISTS = "Group name already exists";
    public static final String MTS_MESSAGE = "[{0}] Message: {1}";
    public static final String MARKET_LEADER_ID_IS_REQUIRED = "MARKET_LEADER_ID_IS_REQUIRED";
    public static final String INVALID_MARKET_LEADER = "INVALID_MARKET_LEADER";
    public static final String INVALID_MARKET_LEADER_ID = "INVALID_MARKET_LEADER_ID";
    public static final String MARKET_LEADER_ID_NOT_FOUND = "MARKET_LEADER_ID_NOT_FOUND";
    public static final String PORTFOLIO_ID_IS_REQUIRED = "PORTFOLIO_ID_IS_REQUIRED";
    public static final String INVALID_PORTFOLIO_ID = "INVALID_PORTFOLIO_ID";
    public static final BigDecimal MARKET_LEADER_NAV_FIRST_FIXED = BigDecimal.valueOf(100000000);
    public static final String MARKET_STOCK_INFO_NOT_FOUND = "Market_stock information is empty, Input params: symbol = {0} ";
    public static final String INVALID_SUB_NUMBER = "INVALID_SUB_NUMBER";
    public static final String INVALID_ACCOUNT_NUMBER = "INVALID_ACCOUNT_NUMBER";
    public static final String INVALID_USERNAME = "INVALID_USERNAME";
    public static final String SUB_NUMBER_HAS_BEEN_SUBSCRIBED_BEFORE = "SUB_NUMBER_HAS_BEEN_SUBSCRIBED_BEFORE";
    public static final String CAN_NOT_SUBSCRIBE_COPY_TRADING_BY_MARGIN_SUB_NUMBER = "CAN_NOT_SUBSCRIBE_COPY_TRADING_BY_MARGIN_SUB_NUMBER";
    public static final String SUB_NUMBER_IS_REQUIRED = "SUB_NUMBER_IS_REQUIRED";
    public static final String SUB_NUMBER_NOT_ALLOW_TO_SUBSCRIBE = "SUB_NUMBER_NOT_ALLOW_TO_SUBSCRIBE";
    public static final String ACCOUNT_NUMBER_IS_REQUIRED = "ACCOUNT_NUMBER_IS_REQUIRED";
    public static final String USERNAME_IS_REQUIRED = "USERNAME_IS_REQUIRED";
    public static final String COPY_TRADE_SUBSCRIBE_SUCCESS = "COPY_TRADE_SUBSCRIBE_SUCCESS";
    public static final String SUB_NUMBER_HAS_NOT_BEEN_SUBSCRIBED_YET = "SUB_NUMBER_HAS_NOT_BEEN_SUBSCRIBED_YET";
    public static final String SUB_NUMBER_HAS_BEEN_UNSUBSCRIBED_BEFORE = "SUB_NUMBER_HAS_BEEN_UNSUBSCRIBED_BEFORE";
    public static final String COPY_TRADE_UNSUBSCRIBE_SUCCESS = "COPY_TRADE_UNSUBSCRIBE_SUCCESS";
    public static final String INACTIVE_MARKET_LEADER_ID = "INACTIVE_MARKET_LEADER_ID";
    public static final String CAN_NOT_VIEW_PORTFOLIO_OF_INACTIVE_MARKET_LEADER = "CAN_NOT_VIEW_PORTFOLIO_OF_INACTIVE_MARKET_LEADER";
    public static final String UTC = "UTC";
    public static final String ORDER_SET_TYPE_IS_REQUIRED = "ORDER_SET_TYPE_IS_REQUIRED";
    public static final String ALLOCATED_RATIO_IS_REQUIRED = "ALLOCATED_RATIO_IS_REQUIRED";
    public static final String DEVICE_UNIQUE_ID_IS_REQUIRED = "DEVICE_UNIQUE_ID_IS_REQUIRED";
    public static final String ACCESS_TOKEN_IS_REQUIRED = "ACCESS_TOKEN_IS_REQUIRED";
    public static final String INVALID_ORDER_SET_TYPE = "INVALID_ORDER_SET_TYPE";
    public static final String INVALID_ALLOCATED_RATIO = "INVALID_ALLOCATED_RATIO";
    public static final String INVALID_ACCESS_TOKEN = "INVALID_ACCESS_TOKEN";
    public static final int DEFAULT_SCALE = 10;
    public static final String STOCK_CODE_REQUIRED = "STOCK_CODE_REQUIRED";
    public static final String PORTFOLIO_SYMBOL_UPLOAD_ITEMS_IS_REQUIRED = "PORTFOLIO_UPLOAD_SYMBOL_ITEMS_IS_REQUIRED";
    public static final String UPLOAD_PORTFOLIO_STOCK_CODE_MUST_NOT_BE_EMPTY = "The stock code for upload must not be empty.";
    public static final String REDIS_KEY_SYMBOL_INFO = "realtime_mapSymbolInfo";
    public static final String UPLOAD_PORTFOLIO_INVALID_STOCK_CODE = "[FAILED] The stock code {0} does not exist on the stock exchange.";
    public static final String UPLOAD_PORTFOLIO_STOCK_CODE_IS_DUPLICATED = "[FAILED] The stock code is duplicated: {0}";
    public static final String DATE_FORMAT_IS_INVALID = "INPUT_DATE_FORMAT_IS_INVALID_CODE";
    public static final String INPUT_DATE_FORMAT_IS_INVALID_MSG = "The format of the date value: {0} must be: {1}";
    public static final String INACTIVE_MARKET_LEADER_ID_MSG = "Market leader id: {0} is inactive";
    public static final String BE_MARKET_LEADER_DATE_INFO_IS_EMPTY = "BE_MARKET_LEADER_DATE_INFO_IS_EMPTY";
    public static final String START_DATE_IS_REQUIRED = "START_DATE_IS_REQUIRED";
    public static final String FIELD_IS_REQUIRED = "FIELD_IS_REQUIRED";
    public static final String FEEDBACK_SENT = "FEEDBACK_SENT";
    public static final String MAX_5_IMAGES = "MAX_5_IMAGES";
    public static final String DAY = "DAY";
    public static final String WEEK = "WEEK";
    public static final String MONTH = "MONTH";
    public static final String YEAR = "YEAR";
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String SUB_00 = "00";
    public static final int PAGE_SIZE_30 = 30;
    public static final int PAGE_SIZE_MAX = 2000;
    public static final String REPORT_DATE_FIELD = "reportDate";
    public static final String ID_FIELD = "id";
    public static final String FROM_DATE_MUST_BE_BEFORE_TODAY = "From date must be before today. Input value is: {0}";
    public static final String SUB_ACCOUNT_NOT_YET_REGISTER = "SUB_ACCOUNT_NOT_YET_REGISTER";
    public static final String ALLOCATED_RATIO = "allocatedRatio";
    public static final String SUB_NUMBER = "subNumber";
    public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
    public static final String INCORRECT_OTP = "INCORRECT_OTP";
    public static final String INPUT_INVALID = "INPUT_INVALID";    
    public static final String OTP_LIMIT_GENERATE = "OTP_LIMIT_GENERATE";
    public static final String INVALID_ID = "INVALID_ID";
    public static final String OTP_KEY_DOES_NOT_EXIST = "OTP_KEY_DOES_NOT_EXIST";
    public static final String OTP_EXPIRED = "OTP_EXPIRED";
    public static final String INCORRECT_OTP_MAX = "INCORRECT_OTP_MAX";
    private Constants() {
    }

    public static class CopyMarketLeaderDetailConstants {
        public static final String TYPE_COPY_TRADING = "COPY_TRADING";
        public static final String LABEL_MARKET_LEADER_SUMMARY_INFO = "MARKET_LEADER_SUMMARY_INFO";
        public static final String KEY_BE_MARKET_LEADER_DATE = "BE_MARKET_LEADER_DATE";
        public static final String KEY_TOTAL_SUB = "TOTAL_SUB";
    }

    public static class DateTimeType {
        public static final String ZONE_DATE_TIME = "ZONE_DATE_TIME";
        public static final String DATE_TIME = "DATE_TIME";
        public static final String DATE = "DATE";
    }

    public static class CacheNames {
        public static final String EXPIRED_IN1_DAY = "EXPIRED_IN1_DAY";
        public static final String EXPIRED_IN15_MINUTES = "EXPIRED_IN15_MINUTES";
        public static final String EXPIRED_IN_JOB_CLEAR = "EXPIRED_IN_JOB_CLEAR";
    }
}
