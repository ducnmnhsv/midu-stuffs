package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.Common;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.NumberValidator;
import com.techx.tradex.common.utils.validator.StringValidator;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketStockQuoteTickRequest extends BaseAfterLoginRequest {

    private String stockCode;
    private int tickUnit;
    private Integer sequence = Common.DEFAULT_MTS_BASE_SEQUENCE_DESC;
    private Integer fetchCount = Common.DEFAULT_MTS_PAGE_SIZE;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("stockCode", this.getStockCode()).empty())
                .add(new NumberValidator("tickUnit", this.getTickUnit()).min(1).eq())
                .add(new NumberValidator("fetchCount", this.getFetchCount()).min(0).eq())
                .check();
    }
}
