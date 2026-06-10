package com.techx.tradex.common.constants;

import com.techx.tradex.common.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Common {
    public static final String SUB_NUMBER_DEFAULT = "00";
    public static final String BANK_CODE_DEFAULT = "9999";
    public static final String BANK_ACCOUNT_DEFAULT = "99999999";
    public static final int DEFAULT_MTS_PAGE_SIZE = 20;
    public static final String DEFAULT_MTS_BASE_STOCK_CODE = "000";
    public static final int DEFAULT_MTS_BASE_SEQUENCE = 0;
    public static final int DEFAULT_MTS_BASE_SEQUENCE_DESC = Integer.MAX_VALUE;
    public static final int DEFAULT_MTS_OFFSET = 0;
    public static final String DEFAULT_MTS_BASE_TIME = "080000";
    public static final String DEFAULT_MTS_BASE_TIME_DESC = "200000";

    public static final String DEFAULT_MTS_BASE_DATE() {
        return new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
    }

    public static final String DEFAULT_MTS_BASE_DATE_FOR_PERIOD() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        return new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
    }

    public static final String DEFAULT_MTS_BASE_TIME_WITH_DATE() {
        return new SimpleDateFormat("yyyyMMddkkmmss").format(TimeUtils.getEndOfDate(new Date()));
    }

}
