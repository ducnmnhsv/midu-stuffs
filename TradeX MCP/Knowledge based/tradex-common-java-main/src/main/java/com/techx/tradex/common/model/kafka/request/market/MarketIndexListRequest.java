package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketIndexListRequest extends BaseAfterLoginRequest {
    private String indexList;

//    public void validate() {
//        new StringValidator("indexList", this.getIndexList()).empty().check();
//    }

}
