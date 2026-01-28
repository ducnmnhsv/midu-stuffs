package com.techx.tradex.realtime.services.impl;

import com.difisoft.model.kafka.Message;
import com.difisoft.model.notification.Configuration;
import com.difisoft.model.notification.MethodEnum;
import com.difisoft.model.notification.NotificationMessage;
import com.difisoft.model.notification.OneSignalConfiguration;
import com.difisoft.model.responses.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.services.CommonService;
import com.techx.tradex.realtime.services.KafkaProducer;
import com.techx.tradex.realtime.utils.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CommonServiceImpl implements CommonService {

    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    @Override
    public void sendNotification(
            String handleId
            , Map<String, Object> messageData
            , String domain
            , MethodEnum methodEnum
            , String templateName
            , Configuration configuration
            , String url
    ) {
        log.info("[{}, message: {}, domain: {}, templateName: {}, configuration: {},"
                , handleId, messageData, domain, templateName
                , CommonUtil.objectToStringJsonIgnoreError(configuration));
        NotificationMessage notificationMsg = new NotificationMessage();
        notificationMsg.setMethod(methodEnum);
        Map<String, Object> template = new HashMap<>();
        template.put(templateName, messageData);
        log.info("[{}], template: {}", handleId,
                CommonUtil.objectToStringJsonIgnoreError(template));

        notificationMsg.setTemplate(template);
        notificationMsg.setDomain(domain);
        notificationMsg.setUrl(url);
        try {
            notificationMsg.setConfiguration(this.objectMapper.writeValueAsString(configuration));
        } catch (JsonProcessingException e) {
            log.error("[{}] fail to prepare message configuration info. Input data: {}"
                    , handleId, CommonUtil.objectToStringJsonIgnoreError(configuration));
        }
        try {
            log.info("[{}], notificationMsg: {}", handleId,
                    CommonUtil.objectToStringJsonIgnoreError(notificationMsg));
            kafkaProducer.sendMessage("notification", "/Update", notificationMsg);
        } catch (Exception e) {
            log.error("[{}] fail to send notification {}, error: {}", handleId, notificationMsg
                    , CommonUtil.objectToStringJsonIgnoreError(e.getStackTrace()));
        }
    }

    @Override
    public OneSignalConfiguration.Filter setOneSignalFilter(String key, String value) {
        OneSignalConfiguration.Filter filter = new OneSignalConfiguration.Filter();
        filter.setKey(key);
        filter.setValue(value);
        filter.setField(OneSignalConfiguration.Field.TAG);
        filter.setRelation(OneSignalConfiguration.Relation.EQUALS);
        return filter;
    }

    @Override
    public <T> T createKafkaRequest(String topic, String appName, String uri, Object request, String prefixLog, TypeReference<Response<T>> clazz) {
        log.info(prefixLog + " -- createKafkaRequest -- topic: {}, uri = {}, request: {}, response type: {}", topic, uri, request, clazz.getType());
        try {
            Message<?> message = kafkaProducer.sendAsyncRequest(topic, uri, appName, request).join();
            log.info(prefixLog + "[createKafkaRequest] message response = {}", CommonUtil.objectToStringJsonIgnoreError(message));
            return message.getResponse(objectMapper, clazz);
        } catch (Exception e) {
            log.error(prefixLog + " -- createKafkaRequest -- uri = {}, request: {}. Error msg: ", uri, request, e);
        }
        return null;
    }

}
