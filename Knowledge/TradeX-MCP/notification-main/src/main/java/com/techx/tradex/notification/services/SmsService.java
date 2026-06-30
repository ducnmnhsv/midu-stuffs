package com.techx.tradex.notification.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.common.model.notification.SmsConfiguration;
import com.techx.tradex.common.utils.LambdaUtils;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.controllers.ResponseProcess;
import com.techx.tradex.notification.dao.SmsDao;
import com.techx.tradex.notification.dao.TemplateDao;
import com.techx.tradex.notification.model.SmsMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SmsService {

    private ObjectMapper objectMapper;
    private TemplateDao templateDao;
    private AppConf appConf;
    private SmsDao smsDao;

    @Autowired
    public SmsService(
            ObjectMapper objectMapper,
            TemplateDao templateDao,
            AppConf appConf,
            SmsDao smsDao
    ) {
        this.objectMapper = objectMapper;
        this.templateDao = templateDao;
        this.appConf = appConf;
        this.smsDao = smsDao;
    }

    @Async
    public void sendSms(NotificationMessage notificationMessage, ResponseProcess<Object> responseProcess) {
        try {
            if (notificationMessage.getTemplate() == null || notificationMessage.getTemplate().isEmpty()) {
                throw new InvalidValueException("template");
            }
            AppConf.SmsServer smsServer = appConf.getSmsServerMap().getOrDefault(notificationMessage.getDomain(), appConf.getSmsServerMap().get(appConf.getDomain()));
            if (smsServer == null) {
                throw new InvalidValueException("domain");
            }
            SmsConfiguration configuration = notificationMessage.getConfiguration(objectMapper, SmsConfiguration.class);
            SmsMessageRequest request = new SmsMessageRequest();
            request.setPhoneNumber(configuration.getPhoneNumber());
            notificationMessage.getTemplate().forEach(LambdaUtils.throwBiConsumer((template, templateData) -> {
                String templateName = template + ".ftl";
                if (notificationMessage.getDomain() != null) {
                    if (notificationMessage.getDomain().equalsIgnoreCase("vcsc") && !templateName.startsWith("vcsc")) {
                        templateName = "vcsc_" + templateName;
                    } else if (notificationMessage.getDomain().equalsIgnoreCase("kis") && !templateName.startsWith("kis")) {
                        templateName = "kis_" + templateName;
                    }
                }
                String content = null;
                if (template.equals("sms_free_content")) {
                    content = ((Map<String,Object>) templateData).get("message").toString();
                } else {
                    content = templateDao.getTemplate(templateName, notificationMessage.getLocale(), templateData);
                }
                request.setContent(content);
                smsDao.sendSms(request, smsServer);
            }));
        } catch (Exception e) {
            if (responseProcess != null) {
                responseProcess.response(null, notificationMessage, e);
            }
        }
    }
}
