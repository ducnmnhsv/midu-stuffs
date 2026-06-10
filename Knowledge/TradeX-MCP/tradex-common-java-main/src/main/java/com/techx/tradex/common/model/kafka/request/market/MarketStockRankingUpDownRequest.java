package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.Common;
import com.techx.tradex.common.constants.MarketTypeEnum;
import com.techx.tradex.common.constants.StockRankingUpDownEnum;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.DefaultUtils;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.EnumValidator;
import com.techx.tradex.common.utils.validator.NumberValidator;
import com.techx.tradex.common.utils.validator.StringValidator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketStockRankingUpDownRequest extends BaseAfterLoginRequest {

    private String marketType;
    private String upDownType;
    private String fromDate = Common.DEFAULT_MTS_BASE_DATE();
    private String toDate = Common.DEFAULT_MTS_BASE_DATE();
    private Integer offSet = Common.DEFAULT_MTS_OFFSET;
    private Integer fetchCount = Common.DEFAULT_MTS_PAGE_SIZE;

    public void validate() {
        new CombineValidator()
                .add(new EnumValidator<>("upDownType", this.upDownType, StockRankingUpDownEnum.class).validate())
                .add(new EnumValidator<>("marketType", this.marketType, MarketTypeEnum.class).validate())
                .add(new StringValidator("fromDate", this.getFromDate())
                        .empty(new Date()).format(DefaultUtils.DATE_FORMAT_VALIDATOR)).
                add(new StringValidator("toDate", this.getToDate())
                        .empty(new Date()).format(DefaultUtils.DATE_FORMAT_VALIDATOR))
                .add(new NumberValidator("fetchCount", this.getFetchCount()).min(0).eq())
                .add(new NumberValidator("offSet", this.getOffSet()).min(0).eq())
                .check();
    }

    public MarketTypeEnum getMarketTypeEnum() {
        return MarketTypeEnum.valueOf(this.marketType);
    }

    public List<MarketTypeEnum> getListMarketTypeEnum() {
        List<MarketTypeEnum> result = new ArrayList<>();
        if (this.marketType.equalsIgnoreCase("ALL")) {
            result.add(MarketTypeEnum.UPCOM);
            result.add(MarketTypeEnum.HOSE);
            result.add(MarketTypeEnum.HNX);
        } else {
            result.add(MarketTypeEnum.valueOf(this.marketType));
        }
        return result;
    }

    public StockRankingUpDownEnum getUpDownEnum() {
        return StockRankingUpDownEnum.valueOf(this.upDownType);
    }

}
