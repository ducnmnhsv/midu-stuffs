package com.techx.tradex.common.model.kafka;

public interface AfterLoginRequest {
    BaseAfterLoginRequest.ConnectionId getConId();

    void setConId(BaseAfterLoginRequest.ConnectionId conId);
}