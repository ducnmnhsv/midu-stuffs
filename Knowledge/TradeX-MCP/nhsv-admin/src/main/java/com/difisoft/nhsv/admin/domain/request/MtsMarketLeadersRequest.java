package com.difisoft.nhsv.admin.domain.request;


import com.difisoft.model.requests.DataRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MtsMarketLeadersRequest extends DataRequest implements BaseRequest {
    @JsonProperty("username")
    public String mlUsername;
    public Integer pageNumber = 0;
    public Integer pageSize = 20;
}
