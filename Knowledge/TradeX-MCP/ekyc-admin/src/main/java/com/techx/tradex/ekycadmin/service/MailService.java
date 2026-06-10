package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.domain.User;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

/**
 * Service for sending emails.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;
    private final AppConf appConf;
    public MailService(
            JHipsterProperties jHipsterProperties,
            JavaMailSender javaMailSender,
            MessageSource messageSource,
            SpringTemplateEngine templateEngine,
            AppConf appConf) {
        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.appConf = appConf;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug(
                "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                isMultipart,
                isHtml,
                to,
                subject,
                content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        } catch (MailException | MessagingException e) {
            log.warn("Email could not be sent to user '{}'", to, e);
        }
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            log.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(User user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/creationEmail", "email.activation.title");
    }

    @Async
    public void sendPasswordResetMail(User user) {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "email.reset.title");
    }

    public void sendCompletedContractEmail(String email, byte[] file, String fileName, String fullName,
            String account) {
        log.info("Sending completed contract email to '{}'", email);
        System.setProperty("mail.mime.splitlongparameters", "false");
        System.setProperty("mail.mime.encodeparameters", "false");
        System.setProperty("mail.mime.allowutf8", "true");
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            message.setTo(email);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("account", account);
            String content = templateEngine.process("mail/openAccountSuccesEmail", context);
            message.setSubject("[NHSV] THÔNG BÁO MỞ TÀI KHOẢN THÀNH CÔNG");
            MimeMultipart mineMutipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(content, "text/html; charset=UTF-8");
            mineMutipart.addBodyPart(messageBodyPart);
            BodyPart attachmentBodyPart = new MimeBodyPart();
            ByteArrayDataSource ds = new ByteArrayDataSource(file, "application/pdf");
            attachmentBodyPart.setDataHandler(new DataHandler(ds));
            attachmentBodyPart.setFileName(MimeUtility.encodeText(fileName, "UTF-8", "B"));
            attachmentBodyPart.setHeader("Content-Type", "application/pdf; name=\"" + fileName + "\"");
            attachmentBodyPart.setHeader("Content-Transfer-Encoding", "base64");
            attachmentBodyPart.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            mineMutipart.addBodyPart(attachmentBodyPart);
            mimeMessage.setContent(mineMutipart);
            javaMailSender.send(mimeMessage);
            log.info("Sent email to email '{}'", email);
        } catch (MailException | MessagingException | UnsupportedEncodingException  e) {
            log.info("Email could not be sent to email '{}'", email, e);
        }
    }
}
