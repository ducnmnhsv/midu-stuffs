package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.Common;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.DefaultUtils;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.NumberValidator;
import com.techx.tradex.common.utils.validator.StringValidator;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketEtfIndexDailyRequest extends BaseAfterLoginRequest {

    private String etfCode;
    private String baseDate = Common.DEFAULT_MTS_BASE_DATE_FOR_PERIOD();
    private Integer fetchCount = Common.DEFAULT_MTS_PAGE_SIZE;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("etfCode", this.getEtfCode()).empty())
                .add(new StringValidator("baseDate", this.getBaseDate())
                        .empty(new Date()).format(DefaultUtils.DATE_FORMAT_VALIDATOR))
                .add(new NumberValidator("fetchCount", this.getFetchCount()).min(0).eq())
                .check();
    }
}
