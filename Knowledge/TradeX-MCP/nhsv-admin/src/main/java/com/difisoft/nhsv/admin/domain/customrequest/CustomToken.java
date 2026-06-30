package com.difisoft.nhsv.admin.domain.customrequest;

import com.difisoft.model.requests.BaseAuthenticatedRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomToken {
    protected String domain;
    protected String userId;
    protected String serviceCode;
    protected BaseAuthenticatedRequest.ConnectionId connectionId;
    protected String serviceId;
    protected String serviceName;
    protected Long clientId;
    protected Long serviceUserId;
    protected Long loginMethod;
    protected Long refreshTokenId;
    protected Long[] scopeGroupIds;
    protected String serviceUsername;
    protected UserData userData;
    protected String platform;
    protected String grantType;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserData {
        protected String username;
        protected String type;
        protected String identifierNumber;
        protected String branchCode;
        protected String mngDeptCode;
        protected String deptCode;
        protected String agencyNumber;
        protected String caThumbprint;
        protected List<String> accountNumbers;
        protected String fssTokenId;
        protected String deviceId;
        protected Long id;
        protected String userLevel;
        protected String name;
        protected String org;
        protected Map<String, List<String>> bankInfo;
    }
}
