package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.Common;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.validator.NumberValidator;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketStockInfoListRequest extends BaseAfterLoginRequest {
    private String stockList;
    private String stockCode = Common.DEFAULT_MTS_BASE_STOCK_CODE;
    private Integer fetchCount = Common.DEFAULT_MTS_PAGE_SIZE;

    public void validate() {
//        new CombineValidator()
//                .add(new StringValidator("stockList", this.getStockList()).empty())
//                .add(new NumberValidator("fetchCount", this.getFetchCount()).min(0).eq())
//                .check();
        new NumberValidator("fetchCount", this.getFetchCount()).min(0).eq().check();
    }

}
