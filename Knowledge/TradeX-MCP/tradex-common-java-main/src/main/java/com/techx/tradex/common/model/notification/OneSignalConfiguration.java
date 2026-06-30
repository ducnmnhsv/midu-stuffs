package com.techx.tradex.common.model.notification;

import com.currencyfair.onesignal.model.notification.NotificationRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OneSignalConfiguration extends NotificationRequest implements Configuration {

    @JsonProperty("include_subscription_ids")
    private List<String> includeSubscriptionIds;

    @Override
    public MethodEnum getMethod() {
        return MethodEnum.ONESIGNAL;
    }
}
