package com.techx.tradex.notification.services;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.controllers.ResponseProcess;
import com.techx.tradex.notification.dao.EmailOneSignalDao;
import com.techx.tradex.notification.dao.TemplateDao;
import com.techx.tradex.notification.model.SmsOneSignalRequest;

import freemarker.template.TemplateNotFoundException;

import com.techx.tradex.notification.model.EmailAddReq;
import com.techx.tradex.notification.model.EmailAddRes;

@Service
public class EmailOneSignalService {
    private static final Logger log = LoggerFactory.getLogger(EmailOneSignalService.class);
    private AppConf appConf;
    private EmailOneSignalDao emailOneSignalDao;
    private TemplateDao templateDao;
    private ObjectMapper objectMapper;

    @Autowired
    public EmailOneSignalService(AppConf appConf, EmailOneSignalDao emailOneSignalDao, TemplateDao templateDao, ObjectMapper objectMapper) {
        this.appConf = appConf;
        this.emailOneSignalDao = emailOneSignalDao;
        this.templateDao = templateDao;
        this.objectMapper = objectMapper;
    }
    
    public EmailAddRes sendEmail(NotificationMessage notificationMessage, ResponseProcess<Object> handleSMSOneSignalResponse){
        try {
            EmailAddReq emailAddReq = notificationMessage.getConfiguration(objectMapper, EmailAddReq.class);
            SmsOneSignalRequest request = new SmsOneSignalRequest();
            if (null == emailAddReq.getTo() || emailAddReq.getTo().isEmpty()) {
                throw new InvalidValueException("to");
            }
            request.setAppID(appConf.getOneSignal().getAppId());
            request.setAppKey(appConf.getOneSignal().getApiKey());
            request.setEmailTo(emailAddReq.getTo());
            request.setSubject(appConf.getEmail().getSubject());
            request.setEmailFromName(appConf.getEmail().getSender());
            LinkedHashMap<String, String> mapOTP = new LinkedHashMap<String, String>();
            mapOTP.put("otp", emailAddReq.getOtp());
//            try {
//                request.setOtp(templateDao.getTemplate("email_otp_verify.html", notificationMessage.getLocale(), mapOTP));
//            } catch (GeneralException e) {
//                if (!(e.getSource() instanceof TemplateNotFoundException)) {
//                    throw e;
//                }
//            }
            String templateHTML = templateDao.getTemplate("email_otp_verify.html", notificationMessage.getLocale(), mapOTP);
            if (templateHTML != null) {
                request.setOtp(templateHTML);
            } else {
                log.error("NOT EXIST HTML TEMPLATE OF " + "email_otp_verify.html");
            }
            emailOneSignalDao.sendEmail(request);
            return EmailAddRes.fromNotification(request.getEmailTo());
        } catch (Exception e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }
    }
}