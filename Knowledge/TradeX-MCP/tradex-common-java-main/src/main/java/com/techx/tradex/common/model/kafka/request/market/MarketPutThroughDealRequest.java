package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.Common;
import com.techx.tradex.common.constants.MarketTypeEnum;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.EnumValidator;
import com.techx.tradex.common.utils.validator.NumberValidator;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketPutThroughDealRequest extends BaseAfterLoginRequest {

    private String marketType;
    private Integer offSet = Common.DEFAULT_MTS_OFFSET;
    private Integer fetchCount = Common.DEFAULT_MTS_PAGE_SIZE;

    public void validate() {
        new CombineValidator()
                .add(new EnumValidator<>("marketType", this.marketType, MarketTypeEnum.class).validate())
                .add(new NumberValidator("fetchCount", this.getFetchCount()).min(0).eq())
                .add(new NumberValidator("offSet", this.getOffSet()).min(0).eq())
                .check();
    }

    public MarketTypeEnum getMarketTypeEnum() {
        return MarketTypeEnum.valueOf(this.marketType);
    }

}
