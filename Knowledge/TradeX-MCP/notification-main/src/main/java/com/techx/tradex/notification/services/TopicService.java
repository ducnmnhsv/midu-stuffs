package com.techx.tradex.notification.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.model.notification.EmailConfiguration;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.common.utils.LambdaUtils;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.constants.TopicMessageType;
import com.techx.tradex.notification.controllers.ResponseProcess;
import com.techx.tradex.notification.dao.EmailDao;
import com.techx.tradex.notification.dao.TemplateDao;
import com.techx.tradex.notification.dao.impl.ZaloDao;
import com.techx.tradex.notification.model.EmailMessageRequest;
import com.techx.tradex.notification.model.db.NotificationList;
import com.techx.tradex.notification.repository.NotificationListRepository;
import freemarker.template.TemplateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class TopicService {
    private AppConf appConf;
    private ZaloDao zaloDao;
    private TemplateDao templateDao;
    private EmailDao emailDao;
    private ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(com.techx.tradex.notification.dao.impl.EmailDao.class);

    @Autowired
    private NotificationListRepository repository;

    @Autowired
    public TopicService(AppConf appConf, ObjectMapper objectMapper, EmailDao emailDao, ZaloDao zaloDao, TemplateDao templateDao) {
        this.appConf = appConf;
        this.emailDao = emailDao;
        this.zaloDao = zaloDao;
        this.templateDao = templateDao;
        this.objectMapper = objectMapper;
    }

    @Async
    public void sendTopicMessage(NotificationMessage notificationMessage, ResponseProcess<Object> responseProcess) {
        try {
            List<NotificationList> notificationList = repository.findByType(notificationMessage.getType());
            EmailConfiguration configuration = notificationMessage.getConfiguration(objectMapper, EmailConfiguration.class);
            Map<String, List<String>> lstEmail = new HashMap<>();
            List<String> to = new ArrayList<>();
            List<String> cc = new ArrayList<>();
            List<String> bcc = new ArrayList<>();
            notificationList.forEach(item -> {
                if (item.getMessage_type().equals(TopicMessageType.ZALO.name())) {
                    try {
                        this.zaloDao.sendMessage(notificationMessage, item.getTo());
                    } catch (Exception e) {
                        log.error("error while send zalo: ", e);
                    }
                } else {
                    int iTo = addData(to, item.getTo());
                    int iBcc = addData(bcc, item.getBcc());
                    int iCc = addData(cc, item.getCc());
                    if (iTo + iBcc + iCc > 44) {
                        doSendMessage(lstEmail, to, cc, bcc, notificationMessage, configuration);
                        to.clear();
                        cc.clear();
                        bcc.clear();
                        lstEmail.clear();
                    }
                }
            });
            doSendMessage(lstEmail, to, cc, bcc, notificationMessage, configuration);
        } catch (Exception e) {
            if (responseProcess != null) {
                responseProcess.response(null, notificationMessage, e);
            }
        }
    }

    private int addData(List<String> lstData, String data) {
        if (data != null && !data.equals("")) {
            lstData.add(data);
        }
        return lstData.size();
    }

    private void doSendMessage(
            Map<String, List<String>> lstEmail,
            List<String> to,
            List<String> cc,
            List<String> bcc,
            NotificationMessage notificationMessage,
            EmailConfiguration configuration
    ) {
        lstEmail.put("to", to);
        lstEmail.put("cc", cc);
        lstEmail.put("bcc", bcc);
        if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty()) {
            return;
        }
        if (lstEmail.get("to").isEmpty()) {
            if (lstEmail.get("cc").isEmpty()) {
                lstEmail.put("to", lstEmail.get("bcc"));
            } else {
                lstEmail.put("to", lstEmail.get("cc"));
            }
        }
        log.info(lstEmail.toString());
        sendEmailMessage(notificationMessage, configuration, lstEmail);
    }

    private void sendEmailMessage(NotificationMessage notificationMessage, EmailConfiguration configuration, Map<String, List<String>> lstEmail) {
        try {
            notificationMessage.getTemplate().forEach(LambdaUtils.throwBiConsumer((template, templateData) -> {
                EmailMessageRequest email = new EmailMessageRequest();
                email.setFrom(appConf.getEmail().getSender());
                email.setRecipients(lstEmail.get("to"));
                email.setCcList(lstEmail.get("cc"));
                email.setBccList(lstEmail.get("bcc"));
                email.setSubject(configuration.getSubject());
//                try {
//                    email.setPlainTextContent(templateDao.getTemplate(template, notificationMessage.getLocale(), templateData));
//                } catch (GeneralException e) {
//                    if (!(e.getSource() instanceof TemplateNotFoundException)) {
//                        throw e;
//                    }
//                }
//                try {
//                    email.setHtmlContent(templateDao.getTemplate(template + ".html", notificationMessage.getLocale(), templateData));
//                } catch (GeneralException e) {
//                    if (!(e.getSource() instanceof TemplateNotFoundException)) {
//                        throw e;
//                    }
//                }
                String templateTl = templateDao.getTemplate(template, notificationMessage.getLocale(), templateData);
                if (templateTl != null) {
                    email.setPlainTextContent(templateTl);
                } else {
                    log.error("NOT EXIST TL TEMPLATE OF " + template);
                }
                String templateHtml = templateDao.getTemplate(template + ".html", notificationMessage.getLocale(), templateData);
                if (templateHtml != null) {
                    email.setHtmlContent(templateHtml);
                } else {
                    log.error("NOT EXIST HTML TEMPLATE OF " + template);
                }
                emailDao.sendMessage(email);
            }));
        } catch (Exception e) {
            log.error("error while send email: ", e);
        }
    }
}
