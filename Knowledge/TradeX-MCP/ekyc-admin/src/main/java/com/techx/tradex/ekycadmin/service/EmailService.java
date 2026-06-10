package com.techx.tradex.ekycadmin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.model.notification.EmailConfiguration;
import com.techx.tradex.common.model.notification.MethodEnum;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.models.templates.EmailKisEkycRegisterFailedTemplate;
import com.techx.tradex.ekycadmin.models.templates.EmailKisEkycRegisterSuccessTemplate;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private ObjectMapper objectMapper;
    private RequestSender requestSender;
    private AppConf appConf;

    private static Map<String, String> successEmailSubjectByLanguage = new HashMap<String, String>() {
        {
            put("vi", "Đăng kí mở tài khoản thành công");
        }
    };

    private static Map<String, String> failedEmailSubjectByLanguage = new HashMap<String, String>() {
        {
            put("vi", "MỞ TÀI KHOẢN KIS BỊ TỪ CHỐI - REGISTER KIS ACCOUNT REJECTED");
        }
    };

    public EmailService(ObjectMapper objectMapper, RequestSender requestSender, AppConf appConf) {
        this.objectMapper = objectMapper;
        this.requestSender = requestSender;
        this.appConf = appConf;
    }

    public void sendApprovedEmail(List<String> emails) throws IOException {
        NotificationMessage message = new NotificationMessage();
        message.setMethod(MethodEnum.EMAIL);
        message.add("email_kis_ekyc_register_success", new EmailKisEkycRegisterSuccessTemplate());
        message.setLocale("vi");
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setToList(emails);
        emailConfiguration.setSubject(successEmailSubjectByLanguage.get(message.getLocale()));
        message.setConfiguration(emailConfiguration, this.objectMapper);
        message.setDomain(appConf.getDomain());
        this.requestSender.sendMessageNoResponse(appConf.getTopics().getNotification(), null, message);
    }

    public void sendRejectedEmail(List<String> emails) throws IOException {
        NotificationMessage message = new NotificationMessage();
        message.setMethod(MethodEnum.EMAIL);
        message.add("email_kis_ekyc_register_failed", new EmailKisEkycRegisterFailedTemplate());
        message.setLocale("vi");
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setToList(emails);
        emailConfiguration.setSubject(failedEmailSubjectByLanguage.get(message.getLocale()));
        message.setConfiguration(emailConfiguration, this.objectMapper);
        message.setDomain(appConf.getDomain());
        this.requestSender.sendMessageNoResponse(appConf.getTopics().getNotification(), null, message);
    }
}
