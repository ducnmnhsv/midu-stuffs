package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.config.ApplicationProperties;
import com.difisoft.nhsv.admin.domain.ChatRoom;
import com.difisoft.nhsv.admin.domain.InviteUser;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.domain.request.FeedbackRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
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
    private static final String CHAT_ROOM = "chatRoom";

    private static final String BASE_URL = "baseUrl";

    private final ApplicationProperties appCof;

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;

    public MailService(
            JHipsterProperties jHipsterProperties,
            JavaMailSender javaMailSender,
            MessageSource messageSource,
            SpringTemplateEngine templateEngine,
            ApplicationProperties appCof) {
        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.appCof = appCof;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.info(
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
            log.info("Sent email to User '{}'", to);
        } catch (MailException | MessagingException e) {
            log.warn("Email could not be sent to user '{}'", to, e);
        }
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            log.info("Email doesn't exist for user '{}'", user.getLogin());
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
    public void sendEmailFromTemplate(InviteUser user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            log.info("Email doesn't exist for user '{}'", user.getLogin());
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
    public void sendChatRoomEmailFromTemplate(User user, ChatRoom chatRoom, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            log.info("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(CHAT_ROOM, chatRoom);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendActivationEmail(User user) {
        log.info("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(InviteUser user) {
        log.info("Sending creation email to '{}'", user.getEmail());
        if (user.getLangKey() == null) {
            user.setLangKey("en");
        }
        if (user.getLangKey().equals("en")) {
            sendEmailFromTemplate(user, "mail/creationEmail_en", "email.activation.title.en");
        } else {
            sendEmailFromTemplate(user, "mail/creationEmail_vi", "email.activation.title.vi");
        }
    }

    @Async
    public void sendPasswordResetMail(User user) {
        log.info("Sending reset password email to '{}'", user.getEmail());
        if (user.getLangKey() == null) {
            user.setLangKey("en");
        }
        if (user.getLangKey().equals("en")) {
            sendEmailFromTemplate(user, "mail/passwordResetEmail_en", "email.reset.title.en");
        } else {
            sendEmailFromTemplate(user, "mail/passwordResetEmail_vi", "email.reset.title.vi");
        }
    }

    public void sendChangePasswordMail(User user) {
        log.info("Sending change password email to '{}'", user.getEmail());
        if (user.getLangKey() == null) {
            user.setLangKey("en");
        }
        if (user.getLangKey().equals("en")) {
            sendEmailFromTemplate(user, "mail/changePasswordEmail_en", "email.change.title.en");
        } else {
            sendEmailFromTemplate(user, "mail/changePasswordEmail_vi", "email.change.title.vi");
        }
    }

    public void sendApproveMail(User user, ChatRoom chatRoom) {
        log.info("Sending approve email to '{}'", user.getEmail());
        if (user.getLangKey() == null) {
            user.setLangKey("en");
        }
        if (user.getLangKey().equals("en")) {
            sendChatRoomEmailFromTemplate(user, chatRoom, "mail/approveEmail_en", "email.approve.title.en");
        } else {
            sendChatRoomEmailFromTemplate(user, chatRoom, "mail/approveEmail_vi", "email.approve.title.vi");
        }
    }

    public void sendRejectEmail(User user, ChatRoom chatRoom) {
        log.info("Sending approve email to '{}'", user.getEmail());
        if (user.getLangKey() == null) {
            user.setLangKey("en");
        }
        if (user.getLangKey().equals("en")) {
            sendChatRoomEmailFromTemplate(user, chatRoom, "mail/rejectEmail_en", "email.reject.title.en");
        } else {
            sendChatRoomEmailFromTemplate(user, chatRoom, "mail/rejectEmail_vi", "email.reject.title.vi");
        }
    }

    public void sendFeedbackEmail(FeedbackRequest request) {
        log.info("Sending feedback email to '{}'", request.getEmail());
        Context context = new Context();
        context.setVariable("email", request.getEmail());
        context.setVariable("message", request.getMessage());
        context.setVariable("fullName", request.getFullName());
        context.setVariable("phoneNo", request.getPhoneNo());
        context.setVariable("href", request.getImageUrl());
        String content = templateEngine.process("mail/feedbackEmail", context);
        String subject = "PHẢN HỒI MTS";
        sendEmail(appCof.getSupportEmail(), subject, content, false, true);
    }

}
