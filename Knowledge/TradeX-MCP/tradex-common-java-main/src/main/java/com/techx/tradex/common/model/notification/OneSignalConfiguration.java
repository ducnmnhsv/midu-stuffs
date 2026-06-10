package com.techx.tradex.common.model.notification;

import com.currencyfair.onesignal.model.notification.NotificationRequest;
import lombok.Data;

@Data
public class OneSignalConfiguration extends NotificationRequest implements Configuration {

    @Override
    public MethodEnum getMethod() {
        return MethodEnum.ONESIGNAL;
    }
}
