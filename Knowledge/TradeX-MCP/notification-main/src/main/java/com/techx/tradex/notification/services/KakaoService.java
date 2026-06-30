package com.techx.tradex.notification.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.common.model.notification.KakaoConfiguration;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.controllers.ResponseProcess;
import com.techx.tradex.notification.dao.KakaoDao;
import com.techx.tradex.notification.dao.TemplateDao;
import com.techx.tradex.notification.model.KakaoMessageRequest;
import com.techx.tradex.notification.model.KakaoMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;

@Service
public class KakaoService {
    private static final Logger log = LoggerFactory.getLogger(KakaoService.class);
    private AppConf appConf;
    private KakaoDao kakaoDao;
    private TemplateDao templateDao;
    private ObjectMapper objectMapper;

    @Autowired
    public KakaoService(AppConf appConf
            , KakaoDao kakaoDao
            , TemplateDao templateDao
            , RequestSender requestSender
            , ObjectMapper objectMapper) {
        this.appConf = appConf;
        this.kakaoDao = kakaoDao;
        this.templateDao = templateDao;
        this.objectMapper = objectMapper;
    }

    @Async
    public void sendMessage(NotificationMessage notificationMessage
            , ResponseProcess<KakaoMessageResponse> responseProcess) {
        KakaoMessageRequest request = new KakaoMessageRequest();
        request.setDeptcode(appConf.getKakao().getCompanyCode());
        request.setUsercode(appConf.getKakao().getId());
        request.setYellowidKey(appConf.getKakao().getYellowIdKey());
        try {
            KakaoConfiguration kakaoConfiguration = notificationMessage.getConfiguration(objectMapper, KakaoConfiguration.class);
            if (notificationMessage.getTemplate() == null || notificationMessage.getTemplate().isEmpty()) {
                throw new InvalidValueException("template");
            }
            notificationMessage.getTemplate().forEach((template, templateData) -> {
                KakaoMessageRequest.Message m = new KakaoMessageRequest.Message();
                request.setMessages(Arrays.asList(m));
                m.setMessageId(UUID.randomUUID().toString());

                m.setTo(kakaoConfiguration.getTo());
                if (StringUtils.isEmpty(m.getTo())) {
                    throw new InvalidValueException("configuration.to");
                }

                m.setText(templateDao.getTemplate(template, notificationMessage.getLocale(), templateData));

                if (!StringUtils.isEmpty(kakaoConfiguration.getFrom())) {
                    m.setFrom(kakaoConfiguration.getFrom());
                }

                m.setReSend(kakaoConfiguration.isResend() ? "Y" : "N");

                if (!StringUtils.isEmpty(kakaoConfiguration.getTemplateCode())) {
                    m.setTemplateCode(kakaoConfiguration.getTemplateCode());
                } else {
                    if (!appConf.getKakao().getTemplateCodes().containsKey(notificationMessage.getTemplate())) {
                        throw new InvalidValueException("templateCode");
                    }
                    m.setTemplateCode(appConf.getKakao().getTemplateCodes().get(notificationMessage.getTemplate()));
                }
            });

            KakaoMessageResponse response = kakaoDao.sendMessage(request);
            log.info("kakao response: {}", response.toString());
            if (responseProcess != null) {
                responseProcess.response(response, notificationMessage, null);
            }
        } catch (Exception e) {
            if (responseProcess != null) {
                responseProcess.response(null, notificationMessage, e);
            }
            return;
        }
    }
}
