package com.techx.tradex.order.model.request;

import com.difisoft.model.requests.DataRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetStopOrderLastUpdateRequest extends DataRequest {
    private String fromTime; //yyyyMMddHHmmss
}
