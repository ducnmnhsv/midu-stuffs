package com.techx.tradex.realtime.services;

import com.difisoft.model.notification.Configuration;
import com.difisoft.model.notification.MethodEnum;
import com.difisoft.model.notification.OneSignalConfiguration;
import com.difisoft.model.responses.Response;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public interface CommonService {

    void sendNotification(
            String handleId
            , Map<String, Object> messageData
            , String domain
            , MethodEnum methodEnum
            , String templateName
            , Configuration configuration
            , String url
    );

    OneSignalConfiguration.Filter setOneSignalFilter(String key, String value);

    <T> T createKafkaRequest(String topic, String appName, String uri, Object request, String prefixLog, TypeReference<Response<T>> clazz);
}
