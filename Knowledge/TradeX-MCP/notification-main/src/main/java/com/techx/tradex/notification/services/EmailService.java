package com.techx.tradex.notification.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.common.model.notification.EmailConfiguration;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.common.utils.LambdaUtils;
import com.techx.tradex.common.utils.StringUtils;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.controllers.ResponseProcess;
import com.techx.tradex.notification.dao.EmailDao;
import com.techx.tradex.notification.dao.TemplateDao;
import com.techx.tradex.notification.model.EmailMessageRequest;
import freemarker.template.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    private AppConf appConf;
    private EmailDao emailDao;
    private TemplateDao templateDao;
    private ObjectMapper objectMapper;

    @Autowired
    public EmailService(AppConf appConf
            , EmailDao emailDao
            , TemplateDao templateDao
            , ObjectMapper objectMapper) {
        this.appConf = appConf;
        this.emailDao = emailDao;
        this.templateDao = templateDao;
        this.objectMapper = objectMapper;
    }

    @Async
    public void sendEmail(NotificationMessage notificationMessage, ResponseProcess<Object> responseProcess) {
        try {
            if (notificationMessage.getTemplate() == null || notificationMessage.getTemplate().isEmpty()) {
                throw new InvalidValueException("template");
            }
            EmailConfiguration configuration = notificationMessage.getConfiguration(objectMapper, EmailConfiguration.class);
            notificationMessage.getTemplate().forEach(LambdaUtils.throwBiConsumer((template, templateData) -> {
                EmailMessageRequest request = new EmailMessageRequest();
                request.setRecipients(configuration.getToList());
                if (StringUtils.isNotEmpty(configuration.getFrom())) {
                    request.setFrom(configuration.getFrom());
                } else {
                    request.setFrom(appConf.getEmail().getSender());
                }
                request.setBccList(configuration.getBccList());
                request.setCcList(configuration.getCcList());
                request.setSubject(configuration.getSubject());
//                try {
                    String templateTl = templateDao.getTemplate(template, notificationMessage.getLocale(), templateData);
                    if (templateTl != null) {
                        request.setPlainTextContent(templateTl);
                    } else {
                        log.warn("NOT EXIST TL TEMPLATE OF " + template);
                    }
//                } catch (GeneralException e) {
//                    if (!(e.getSource() instanceof TemplateNotFoundException)) {
//                        throw e;
//                    }
//                }
//                try {
                    String templateHtml = templateDao.getTemplate(template + ".html", notificationMessage.getLocale(), templateData);
                    if (templateHtml != null) {
                        request.setHtmlContent(templateHtml);
                    } else {
                        log.warn("NOT EXIST HTML TEMPLATE OF " + template);
                    }
//                } catch (GeneralException e) {
//                    if (!(e.getSource() instanceof TemplateNotFoundException)) {
//                        throw e;
//                    }
//                }
                emailDao.sendMessage(request);
            }));

        } catch (Exception e) {
            if (responseProcess != null) {
                responseProcess.response(null, notificationMessage, e);
            }
        }
    }
}
