package com.techx.tradex.common.model.notification;

import lombok.Data;

@Data
public class SocketClusterConfiguration implements Configuration {
    private String channel;
    private SocketClusterData message;

    @Override
    public MethodEnum getMethod() {
        return MethodEnum.SOCKET_CLUSTER;
    }
}
