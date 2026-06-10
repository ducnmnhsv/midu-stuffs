package com.techx.tradex.common.model.notification;

import lombok.Data;

@Data
public class KakaoConfiguration implements Configuration {
    private String to;
    private String from;
    private boolean resend;
    private String templateCode;

    @Override
    public MethodEnum getMethod() {
        return MethodEnum.KAKAO;
    }
}
