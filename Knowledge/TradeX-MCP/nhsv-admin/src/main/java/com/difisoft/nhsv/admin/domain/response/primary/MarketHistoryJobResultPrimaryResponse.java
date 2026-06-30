package com.difisoft.nhsv.admin.domain.response.primary;

import com.difisoft.nhsv.admin.domain.response.MarketHistoryJobResultResponse;
import lombok.Data;
import org.springframework.context.annotation.Primary;

@Data
@Primary
public class MarketHistoryJobResultPrimaryResponse extends MarketHistoryJobResultResponse {
    private String eventId;
    private String eventName;
    private String eventType;
}
