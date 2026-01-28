package com.techx.tradex.realtime.model.request;

import com.difisoft.model.requests.DataRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndexRankRequest extends DataRequest {
    private String index;
    private String type;
    private Integer pageNumber;
    private Integer pageSize;
}
