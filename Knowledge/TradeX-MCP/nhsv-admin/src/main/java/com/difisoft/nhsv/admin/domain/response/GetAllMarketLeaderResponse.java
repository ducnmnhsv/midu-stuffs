package com.difisoft.nhsv.admin.domain.response;

import lombok.Data;

@Data
public class GetAllMarketLeaderResponse {
    private Long marketLeaderId;
    private String username;
    private String fullname;
    private String imageUrl;
    private Double profitLossRatio;
    private Long totalSubscribers;
}
