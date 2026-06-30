package com.techx.tradex.notification.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.InvalidFormatException;
import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.common.model.notification.SmsConfiguration;
import com.techx.tradex.common.utils.LambdaUtils;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.controllers.ResponseProcess;
import com.techx.tradex.notification.dao.SmsOneSignalDao;
import com.techx.tradex.notification.dao.TemplateDao;
import com.techx.tradex.notification.model.SmsOneSignalRequest;
import com.techx.tradex.notification.model.PhoneNumberAddReq;
import com.techx.tradex.notification.model.PhoneNumberAddRes;

@Service
public class SmsOneSignalService {
    private ObjectMapper objectMapper;
    private TemplateDao templateDao;
    private AppConf appConf;
    private SmsOneSignalDao smsOneSignalDao;
    private static final Logger logger = LoggerFactory.getLogger(SmsOneSignalService.class);

    @Autowired
    public SmsOneSignalService(ObjectMapper objectMapper, TemplateDao templateDao, AppConf appConf,
        	SmsOneSignalDao smsOneSignalDao) {
        this.objectMapper = objectMapper;
        this.templateDao = templateDao;
        this.appConf = appConf;
        this.smsOneSignalDao = smsOneSignalDao;
    }

    @Async
    public void sendSMS(NotificationMessage notificationMessage, ResponseProcess<Object> handleSMSOneSignalResponse) {
        try {
            if (notificationMessage.getTemplate() == null || notificationMessage.getTemplate().isEmpty()) {
                throw new InvalidValueException("template");
            }
            AppConf.OneSignalApp oneSignalApp = appConf.getOneSignalMap().getOrDefault(notificationMessage.getDomain(),
                    null);
            if (oneSignalApp == null) {
                throw new InvalidValueException("domain");
            }
            SmsConfiguration configuration = notificationMessage.getConfiguration(objectMapper, SmsConfiguration.class);
            PhoneNumberAddReq phoneNumberReq = new PhoneNumberAddReq();
            phoneNumberReq.setPhoneNumber(configuration.getPhoneNumber());
            registerPhoneNumber(phoneNumberReq);
            SmsOneSignalRequest request = new SmsOneSignalRequest();
            request.setAppID(oneSignalApp.getAppId());
            request.setAppKey(oneSignalApp.getApiKey());
            request.setPhoneFrom(appConf.getSmsOneSignal().getPhoneFrom());
            request.setPhoneTo(configuration.getPhoneNumber());
            notificationMessage.getTemplate().forEach(LambdaUtils.throwBiConsumer((template, templateData) -> {
                String templateName = template + ".ftl";
                if (notificationMessage.getDomain().equalsIgnoreCase("vcsc") && !templateName.startsWith("vcsc")) {
                    templateName = "vcsc_" + templateName;
                } else if (notificationMessage.getDomain().equalsIgnoreCase("kis") && !templateName.startsWith("kis")) {
                    templateName = "kis_" + templateName;
                }
                String content = templateDao.getTemplate(templateName, notificationMessage.getLocale(), templateData);
                request.setContent(content);
                smsOneSignalDao.sendSms(request);
            }));
        } catch (Exception e) {
            if (handleSMSOneSignalResponse != null) {
                handleSMSOneSignalResponse.response(null, notificationMessage, e);
            }
        }
    }

    public PhoneNumberAddRes registerPhoneNumber(PhoneNumberAddReq phoneNumberAddReq) {
        SmsOneSignalRequest request = new SmsOneSignalRequest();
        if (null == phoneNumberAddReq.getPhoneNumber() || phoneNumberAddReq.getPhoneNumber().isEmpty()) {
            throw new InvalidValueException("phoneNumber");
        }
        request.setAppID(appConf.getOneSignal().getAppId());
        request.setAppKey(appConf.getOneSignal().getApiKey());
        request.setPhoneTo(phoneNumberAddReq.getPhoneNumber());
        smsOneSignalDao.registerPhoneNumber(request);
        return PhoneNumberAddRes.fromNotification(request.getPhoneTo());
    }
}
