package com.difisoft.nhsv.admin.domain.request;

import com.difisoft.model.requests.DataRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketLeaderProfitLossRequest extends DataRequest implements BaseRequest {
    private String marketLeaderId;
    private String fromDate;
    private String toDate;
    private Boolean sortAsc = true;
    private Integer pageNumber = 0;
    private Integer pageSize = 20;

    public String objToString() {
        return MessageFormat.format("{0}_{1}_{2}_{3}_{4}_{5}", marketLeaderId, fromDate, toDate, sortAsc, pageNumber, pageSize);
    }
}
