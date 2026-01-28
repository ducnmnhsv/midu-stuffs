package com.techx.tradex.common.model.notification;

import lombok.Data;

@Data
public class SocketClusterData implements TemplateData {
    private String method;
    private Object payload;

    @Override
    public String getTemplate() {
        return "socket_cluster_template";
    }
}
