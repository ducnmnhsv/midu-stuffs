package com.techx.tradex.notification.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.InvalidFormatException;
import com.techx.tradex.common.kafka.KafkaRequestHandlerAndSender;
import com.techx.tradex.common.model.kafka.Message;
import com.techx.tradex.common.model.kafka.MessageTypeEnum;
import com.techx.tradex.common.model.notification.MethodEnum;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.model.KakaoMessageResponse;
import com.techx.tradex.notification.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RequestHandler extends KafkaRequestHandlerAndSender {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private ObjectMapper objectMapper;
    private KakaoService kakaoService;
    private EmailService emailService;
    private OneSignalService oneSignalService;
    private SocketClusterService socketClusterService;
    private SmsService smsService;
    private TopicService topicService;
    private SmsOneSignalService smsOneSignalService;
    private EmailOneSignalService emailOneSignalService;
    private ResponseProcess<KakaoMessageResponse> handleKakaoResponse = (messageResponse, notificationMessage, exception) -> {
        if (exception != null) {
            logger.error("error while sending kakao message {}", notificationMessage.toString(), exception);
        }
    };
    private ResponseProcess<Object> handleEmailResponse = (messageResponse, notificationMessage, exception) -> {
        if (exception != null) {
            logger.error("error while sending email message {}", notificationMessage.toString(), exception);
        }
    };
    private ResponseProcess<Object> handleTopicResponse = (messageResponse, notificationMessage, exception) -> {
        if (exception != null) {
            logger.error("error while sending message {}", notificationMessage.toString(), exception);
        }
    };
    private ResponseProcess<Object> handleOneSignalResponse = (messageResponse, notificationMessage, exception) -> {
        if (exception != null) {
            logger.error("error while sending OneSignal notification {}", notificationMessage.toString(), exception);
        }
    };
    private ResponseProcess<Object> handleSocketClusterResponse = (messageResponse, notificationMessage, exception) -> {
        if (exception != null) {
            logger.error("error while sending socket message {}", notificationMessage.toString(), exception);
        }
    };
    private ResponseProcess<Object> handleSmsResponse = (messageResponse, notificationMessage, exception) -> {
        if (exception != null) {
            logger.error("error while sending sms message {}", notificationMessage.toString(), exception);
        }
    };
    private ResponseProcess<Object> handleSMSOneSignalResponse = (messageResponse, notificationMessage, exception) -> {
        if (exception != null) {
            logger.error("error while sending SMS OneSignal message {}", notificationMessage.toString(), exception);
        }
    };
    private ResponseProcess<Object> handleEmailOneSignalResponse = (messageResponse, notificationMessage, exception) -> {
        if (exception != null) {
            logger.error("error while sending email OneSignal message {}", notificationMessage.toString(), exception);
        }
    };

    @Autowired
    public RequestHandler(
            AppConf appConf
            , KakaoService kakaoService
            , EmailService emailService
            , ObjectMapper objectMapper
            , TopicService topicService
            , OneSignalService oneSignalService
            , SocketClusterService socketClusterService
            , RequestSender requestSender
            , SmsService smsService
            , SmsOneSignalService smsOneSignalService
            , EmailOneSignalService emailOneSignalService) {
        this.kakaoService = kakaoService;
        this.emailService = emailService;
        this.oneSignalService = oneSignalService;
        this.socketClusterService = socketClusterService;
        this.smsService = smsService;
        this.topicService = topicService;
        this.objectMapper = objectMapper;
        this.smsOneSignalService = smsOneSignalService;
        this.emailOneSignalService = emailOneSignalService;
        List<String> topics = new ArrayList<>();
        topics.add(appConf.getClusterId());
        if (appConf.getDomain().equals("tradex")) {
            topics.add(appConf.getTradexOnlyRequestHandler());
        }
        this.init(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), appConf.getNodeId(), topics, 1, false);
        requestSender.setRequestSender(this.sender());
    }

    @Override
    protected Object handle(Message message) {
        if (message.getMessageType() == MessageTypeEnum.REQUEST || message.getMessageType() == MessageTypeEnum.MESSAGE) {
            NotificationMessage notificationMessage;
            try {
                notificationMessage = Message.getData(objectMapper, message, NotificationMessage.class);
            } catch (IOException e) {
                logger.error("fail to parse message to notification {} ", message, e);
                throw new InvalidFormatException("message");
            }
            if (notificationMessage.getMethod() == MethodEnum.KAKAO) {
                kakaoService.sendMessage(notificationMessage, this.handleKakaoResponse);
            } else if (notificationMessage.getMethod() == MethodEnum.EMAIL) {
                emailService.sendEmail(notificationMessage, this.handleEmailResponse);
            } else if (notificationMessage.getMethod() == MethodEnum.ONESIGNAL) {
                oneSignalService.sendNotification(notificationMessage, this.handleOneSignalResponse);
            } else if (notificationMessage.getMethod() == MethodEnum.SOCKET_CLUSTER) {
                socketClusterService.publish(notificationMessage, this.handleSocketClusterResponse);
            } else if (notificationMessage.getMethod() == MethodEnum.SMS) {
                smsService.sendSms(notificationMessage, this.handleSmsResponse);
            } else if (notificationMessage.getMethod() == MethodEnum.TOPIC) {
                topicService.sendTopicMessage(notificationMessage, this.handleTopicResponse);
            } else if (notificationMessage.getMethod() == MethodEnum.ONESIGNAL_SMS) {
                smsOneSignalService.sendSMS(notificationMessage, this.handleSMSOneSignalResponse);
            } else if (notificationMessage.getMethod() == MethodEnum.ONESIGNAL_EMAIL) {
                emailOneSignalService.sendEmail(notificationMessage, this.handleEmailOneSignalResponse);
            }

        }
        return Observable.from(new String[]{});
    }
}
