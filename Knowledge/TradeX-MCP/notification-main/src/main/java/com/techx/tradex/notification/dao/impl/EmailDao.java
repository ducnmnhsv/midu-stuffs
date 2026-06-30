package com.techx.tradex.notification.dao.impl;

import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.common.utils.StringUtils;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.model.EmailMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Repository;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Repository
public class EmailDao implements com.techx.tradex.notification.dao.EmailDao {
    private static final Logger log = LoggerFactory.getLogger(EmailDao.class);
    private static final String ENCODING = "UTF-8";

    @Autowired
    AppConf appConf;

    @Override
    public void sendMessage(EmailMessageRequest request) throws Exception {
        JavaMailSenderImpl javaMailSender = getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        int type = 0;
        System.out.println(request.getHtmlContent());
        if (StringUtils.isNotEmpty(request.getPlainTextContent())) {
            type++;
        }
        if (StringUtils.isNotEmpty(request.getHtmlContent())) {
            type += 2;
        }
        if (type == 0) {
            throw new InvalidValueException("content");
        }
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, type == 3, ENCODING);
        helper.setTo(request.getRecipients().toArray(new String[request.getRecipients().size()]));
        helper.setSubject(request.getSubject());
        helper.setFrom(StringUtils.isEmpty(request.getFrom()) ? appConf.getEmail().getSender() : request.getFrom());
        switch (type) {
            case 1:
                helper.setText(request.getPlainTextContent(), false);
                break;
            case 2:
                helper.setText(request.getHtmlContent(), true);
                break;
            case 3:
                helper.setText(request.getPlainTextContent(), request.getHtmlContent());
                break;
        }
        javaMailSender.send(helper.getMimeMessage());
    }

    private JavaMailSenderImpl getMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(appConf.getEmail().getEndpoint());
        mailSender.setPort(appConf.getEmail().getPort());
//        mailSender.setDefaultEncoding(mailConfiguration.getEncoding());
        mailSender.setUsername(appConf.getEmail().getSmtpUsername());
        mailSender.setPassword(appConf.getEmail().getSmtpPassword());

        Properties prop = mailSender.getJavaMailProperties();
        prop.put("mail.transport.protocol", "smtp");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.ssl.trust", appConf.getEmail().getEndpoint());

        prop.put("mail.smtp.starttls.required", "true");
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return mailSender;
    }
}
