package com.techx.tradex.notification.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.model.kafka.Message;
import com.techx.tradex.common.model.kafka.MessageTypeEnum;
import com.techx.tradex.common.model.notification.*;
import com.techx.tradex.notification.controllers.RequestHandler;
import com.techx.tradex.notification.services.OneSignalService;
import com.techx.tradex.notification.services.SocketClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ConditionalOnProperty(name = "testService")
public class TestService implements ApplicationRunner {
    @Autowired
    RequestHandler requestHandler;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OneSignalService oneSignalService;
    @Autowired
    SocketClusterService socketClusterService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        testKisEkycRegisterSuccess();
        testKisEkycRegisterFailed();
//        testSendNotification();
//        testSendSocketCluster();
    }

    public void testSendNotification() throws Exception {
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setMethod(MethodEnum.ONESIGNAL);
        Message message = new Message();
        message.setMessageType(MessageTypeEnum.REQUEST);
        message.setData(notificationMessage);

        OneSignalConfiguration configuration = new OneSignalConfiguration();
        List<String> includedSegments = new ArrayList<>();
        includedSegments.add("All");
        configuration.setIncludedSegments(includedSegments);
        notificationMessage.setConfiguration(configuration, objectMapper);

        AlarmNotificationData alarmNotificationData = new AlarmNotificationData();
        alarmNotificationData.setCode("AAA");
        alarmNotificationData.setValue(10d);
        notificationMessage.add(alarmNotificationData.getTemplate(), alarmNotificationData);
        requestHandler.process(message);
    }

    public void testVerificationEmailVn() throws Exception {
        Message message = new Message();
        message.setMessageType(MessageTypeEnum.REQUEST);
        NotificationMessage notificationMessage = new NotificationMessage();
        message.setData(notificationMessage);
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setToList(Arrays.asList("cuong.9312@gmail.com"));
        emailConfiguration.setSubject("test verification email");
        notificationMessage.setConfiguration(emailConfiguration, objectMapper);
        notificationMessage.setLocale("vi");
        EmailVerificationData emailVerificationData = new EmailVerificationData();
        emailVerificationData.setActivationCode("095736");
        emailVerificationData.setExpirationTime("2018-08-9 12:00:00 +07:00");
        emailVerificationData.setBaseUrl("localhost");
        emailVerificationData.setUsername("cuongth");
        notificationMessage.add(emailVerificationData.getTemplate(), emailVerificationData);
        requestHandler.process(message);
    }

    public void testKisEkycRegisterSuccess() throws Exception {
        Message message = new Message();
        message.setMessageType(MessageTypeEnum.REQUEST);
        NotificationMessage notificationMessage = new NotificationMessage();
        message.setData(notificationMessage);
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setToList(Arrays.asList("trang.pham@techx.vn"));
        emailConfiguration.setSubject("Đăng ký mở tài khoản thành công");
        emailConfiguration.setFrom("hopdong@kisvn.vn"); // if null -> get from config
        notificationMessage.setConfiguration(emailConfiguration, objectMapper);
        notificationMessage.setLocale("ko");
        Map<String, String> data = new HashMap<>();
        notificationMessage.add("email_kis_ekyc_register_success", data);
        System.out.println(objectMapper.writeValueAsString(message));
        requestHandler.process(message);
    }

    public void testKisEkycRegisterFailed() throws Exception {
        Message message = new Message();
        message.setMessageType(MessageTypeEnum.REQUEST);
        NotificationMessage notificationMessage = new NotificationMessage();
        message.setData(notificationMessage);
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setToList(Arrays.asList("trang.pham@techx.vn"));
        emailConfiguration.setSubject("Đăng ký mở tài khoản thất bại");
        emailConfiguration.setFrom("hopdong@kisvn.vn"); // if null -> get from config
        notificationMessage.setConfiguration(emailConfiguration, objectMapper);
        notificationMessage.setLocale("ko");
        Map<String, String> data = new HashMap<>();
        notificationMessage.add("email_kis_ekyc_register_failed", data);
        System.out.println(objectMapper.writeValueAsString(message));
        requestHandler.process(message);
    }

    public void testKbfinaOtp() throws Exception {
        Message message = new Message();
        message.setMessageType(MessageTypeEnum.REQUEST);
        NotificationMessage notificationMessage = new NotificationMessage();
        message.setData(notificationMessage);
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setToList(Arrays.asList("cuong.9312@gmail.com"));
        emailConfiguration.setSubject("OTP");
        notificationMessage.setConfiguration(emailConfiguration, objectMapper);
        notificationMessage.setLocale("en");
        Map<String, String> data = new HashMap<>();
        data.put("otp", "123122");
        notificationMessage.add("email_kbfina_otp", data);
        requestHandler.process(message);
    }

    public void testSendSocketCluster() throws Exception {
        Message message = new Message();
        message.setMessageType(MessageTypeEnum.REQUEST);
        NotificationMessage notificationMessage = new NotificationMessage();
        message.setData(notificationMessage);
        SocketClusterConfiguration clusterConfiguration = new SocketClusterConfiguration();
        clusterConfiguration.setChannel("market.index.quote.XXX");
        SocketClusterData data = new SocketClusterData();
        data.setMethod("LOGIN");
        data.setPayload("Login notify");
        notificationMessage.setConfiguration(clusterConfiguration, objectMapper);
        notificationMessage.add("socket_cluster_template", data);
        requestHandler.process(message);
//        socketClusterService.publish("market.index.quote.XXX", "ddddđ");
    }
}
