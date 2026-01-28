package com.techx.tradex.common.model.kafka;

import lombok.Data;

@Data
public class BaseAfterLoginRequest implements AfterLoginRequest {
    protected ConnectionId conId = new ConnectionId();

    @Data
    public static class ConnectionId {
        private String connectionId;
        private String serviceId;
        private String serviceName;
    }
}
