package com.techx.tradex.order.model.request;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.requests.DataRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopOrderHistoryRequest extends DataRequest {
    private String accountNumber;
    private String subNumber;  //optional
    private String code; //optional
    private String sellBuyType; //optional
    private String orderType; //optional
    private String status; //optional
    private Long lastStopOrderId; //optional
    private Integer fetchCount; //optional
    private String fromDate; //optional
    private String toDate; //optional

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType == null ? null : SellBuyTypeEnum.valueOf(this.sellBuyType);
    }

    @Override
    public String getUsername() {
        if (this.getHeaders() != null
                && this.getHeaders().getToken() != null
                && this.getHeaders().getToken().getUserData() != null) {
            return this.getHeaders().getToken().getUserData().getUsername();
        }
        return null;
    }

    public String getType() {
        if (this.getHeaders() != null
                && this.getHeaders().getToken() != null
                && this.getHeaders().getToken().getUserData() != null) {
            return this.getHeaders().getToken().getUserData().getType();
        }
        return null;
    }
}
