package com.difisoft.nhsv.admin.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriberInformationResponse {
    private String username;
    private String accountNumber;
    private String subNumber;
    private MarketLeaderinfo marketLeaderInfo;
    private String subscribedDateTime;
    private String unsubscribedDateTime;
    private String copyTradingStatus;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MarketLeaderinfo {
        private Long marketLeaderId;
        private String marketLeaderUsername;
        private String marketLeaderFullname;
        private String marketLeaderStatus;
        private String marketLeaderImageUrl;
        private Double profitLossRatio;
        private Long totalSubscribers;
    }

}
