package com.difisoft.nhsv.admin.domain.request;

import com.difisoft.model.requests.DataRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketLeaderPeriodProfitLossRequest extends DataRequest implements BaseRequest {
    private List<Long> marketLeaderIds;
    private String period;
    private Boolean sortAsc = true;
    private Integer pageNumber = 0;
    private Integer pageSize = 20;
}
