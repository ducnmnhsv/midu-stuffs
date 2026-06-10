package com.techx.tradex.common.model.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {
    protected String domain;
    protected String userId;
    protected String serviceCode;
    protected BaseAfterLoginRequest.ConnectionId connectionId;
    protected String serviceId;
    protected String serviceName;
    protected Long clientId;
    protected Long serviceUserId;
    protected Long loginMethod;
    protected Long refreshTokenId;
    protected Long[] scopeGroupIds;
    protected String serviceUsername;
    protected UserData userData;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserData {
        protected String username;
        protected String identifierNumber;
        protected String branchCode;
        protected String mngDeptCode;
        protected String deptCode;
        protected String agencyNumber;
        protected String caThumbprint;
        protected List<String> accountNumbers;
        protected String fssTokenId;
        protected String deviceId;
    }
}
