package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.Common;
import com.techx.tradex.common.constants.PeriodType;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.DefaultUtils;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.EnumValidator;
import com.techx.tradex.common.utils.validator.NumberValidator;
import com.techx.tradex.common.utils.validator.StringValidator;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketStockPeriodRequest extends BaseAfterLoginRequest {
    private String stockCode;
    private String periodType;
    private String baseDate = Common.DEFAULT_MTS_BASE_DATE_FOR_PERIOD();
    private boolean adjustedPriceType;
    private Integer fetchCount = Common.DEFAULT_MTS_PAGE_SIZE;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("stockCode", this.getStockCode()).empty())
                .add(new StringValidator("baseDate", this.getBaseDate())
                        .empty(new Date()).format(DefaultUtils.DATE_FORMAT_VALIDATOR))
                .add(new NumberValidator("fetchCount", this.getFetchCount()).min(0).eq())
                .add(new EnumValidator<>("periodType", this.periodType, PeriodType.class).validate())
                .check();
    }
}
