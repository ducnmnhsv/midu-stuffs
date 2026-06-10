package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.Common;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.NumberValidator;
import com.techx.tradex.common.utils.validator.StringValidator;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketIndexQuoteTickRequest {

    private String indexCode;
    private int tickUnit;
    private Integer sequence = Common.DEFAULT_MTS_BASE_SEQUENCE_DESC;
    private Integer fetchCount = Common.DEFAULT_MTS_PAGE_SIZE;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("indexCode", this.getIndexCode()).empty())
                .add(new NumberValidator("tickUnit", this.getTickUnit()).min(1).eq())
                .add(new NumberValidator("fetchCount", this.getFetchCount()).min(0).eq())
                .check();
    }
}
