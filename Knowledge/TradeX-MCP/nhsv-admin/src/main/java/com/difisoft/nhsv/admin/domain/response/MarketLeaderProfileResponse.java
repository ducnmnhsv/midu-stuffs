package com.difisoft.nhsv.admin.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketLeaderProfileResponse {
    private Long marketLeaderId;
    private String username;
    private String fullname;
    private String introduction;
    private String imageUrl;
    private String email;
    private String activatedAt;
    private String status;
    private String deactivatedAt;
    private String deactivatedBy;
    private String invitedBy;
    private Long totalSubscribers;
    private Double profitLossRatio;
}
