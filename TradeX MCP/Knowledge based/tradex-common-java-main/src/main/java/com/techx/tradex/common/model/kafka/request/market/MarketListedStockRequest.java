package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.Common;
import com.techx.tradex.common.constants.MarketTypeEnum;
import com.techx.tradex.common.constants.StockSecuritiesTypeEnum;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.EnumValidator;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketListedStockRequest extends BaseAfterLoginRequest {

    private String marketType;
    private String securitiesType;
    private String stockCode = Common.DEFAULT_MTS_BASE_STOCK_CODE;
    private Integer fetchCount = Common.DEFAULT_MTS_PAGE_SIZE;

    public void validate() {
        new CombineValidator()
                .add(new EnumValidator<>("marketType", this.marketType, MarketTypeEnum.class).validate())
                .add(new EnumValidator<>("securitiesType", this.securitiesType, StockSecuritiesTypeEnum.class).validate())
                .check();
    }

    public String getStockCode() {
        if (stockCode == null) {
            stockCode = "";
        }
        return stockCode;
    }

    public MarketTypeEnum getMarketTypeEnum() {
        return MarketTypeEnum.valueOf(this.marketType);
    }

    public StockSecuritiesTypeEnum getSecuritiesTypeEnum() {
        return StockSecuritiesTypeEnum.valueOf(this.securitiesType);
    }

}
