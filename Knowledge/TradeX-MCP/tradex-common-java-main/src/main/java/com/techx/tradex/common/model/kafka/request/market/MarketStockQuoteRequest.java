package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.Common;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.DefaultUtils;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.NumberValidator;
import com.techx.tradex.common.utils.validator.StringValidator;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketStockQuoteRequest extends BaseAfterLoginRequest {

    private String stockCode;
    private String baseTime = Common.DEFAULT_MTS_BASE_TIME_WITH_DATE();
    private Integer sequence = Common.DEFAULT_MTS_BASE_SEQUENCE_DESC;
    private Integer fetchCount = Common.DEFAULT_MTS_PAGE_SIZE;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("stockCode", this.getStockCode()).empty())
                .add(new NumberValidator("fetchCount", this.getFetchCount()).min(0).eq())
                .add(new StringValidator("baseTime", this.getBaseTime()).format(DefaultUtils.DATETIME_FORMAT_VALIDATOR))
                .check();
    }

    public String getTimeFromBaseTime() {
        return this.baseTime.substring(8, this.baseTime.length());
    }
}
