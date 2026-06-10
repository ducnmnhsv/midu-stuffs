package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.validator.StringValidator;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketFutureCurrentPriceRequest extends BaseAfterLoginRequest {
    private String stockCode;

    public void validate() {
        new StringValidator("stockCode", this.getStockCode()).empty();
    }
}
