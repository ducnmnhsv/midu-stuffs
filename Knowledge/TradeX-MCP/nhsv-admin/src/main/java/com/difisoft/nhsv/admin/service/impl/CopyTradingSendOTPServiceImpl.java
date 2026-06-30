package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.exceptions.InvalidValueException;
import com.difisoft.model.exceptions.SubErrorsException;
import com.difisoft.nhsv.admin.config.AppConf;
import com.difisoft.nhsv.admin.config.SoapClient;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.request.CopyTradingSendOTPRequest;
import com.difisoft.nhsv.admin.domain.request.CopyTradingVerifyOTPRequest;
import com.difisoft.nhsv.admin.domain.response.CopyTradingSendOTPGetPhoneResponse;
import com.difisoft.nhsv.admin.domain.response.CopyTradingSendOTPResponse;
import com.difisoft.nhsv.admin.domain.response.CopyTradingVerifyOTPResponse;
import com.difisoft.nhsv.admin.domain.vn._1sms.SendMT;
import com.difisoft.nhsv.admin.domain.vn._1sms.SendMTResponse;
import com.difisoft.nhsv.admin.service.CopyTradingSendOTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CopyTradingSendOTPServiceImpl implements CopyTradingSendOTPService {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private final StringRedisTemplate redisTemplate;
    private final SoapClient soapClient;
    private final AppConf appConf;
    @Value("${app.rootURL}")
    private String rootURL;
    @Value("${app.nhsvConfig.headers.api-key}")
    private String apiKey;
    @Value("${app.nhsvConfig.messageOtp.otpMessagePrefixVI}")
    private String otpMessagePrefixVI;
    @Value("${app.nhsvConfig.messageOtp.otpMessageSuffixVI}")
    private String otpMessageSuffixVI;
    @Value("${app.nhsvConfig.messageOtp.otpMessagePrefixEN}")
    private String otpMessagePrefixEN;
    @Value("${app.nhsvConfig.messageOtp.otpMessageSuffixEN}")
    private String otpMessageSuffixEN;


    @Override
    public CopyTradingSendOTPResponse generateOtp(CopyTradingSendOTPRequest request, RequestContext<CopyTradingSendOTPRequest> ctx) {

        log.info("[verifyOtp] ctxId: {}, copyTradingVerifyOTPRequest: {}", ctx.getId(), request);

        List<String> accountNumbers = request.getAccountNumbers();
        log.info("Account numbers: {}", accountNumbers);

        String phoneNumberByAccount = getPhoneNumber(accountNumbers.get(0));
        log.info("Phone number for the first account number ({}): {}", accountNumbers.get(0), phoneNumberByAccount);
        String phoneNumber = request.getId();
        if (phoneNumber == null || !phoneNumberByAccount.equals(request.getId())) {
            throw new SubErrorsException(Constants.INVALID_PARAMETER).add(Constants.INVALID_ID, "id", Collections.singletonList(request.getId()));
        }

        phoneNumber = "84" + (phoneNumber.startsWith("0") ? phoneNumber.substring(1) : phoneNumber);
        String otpKey = UUID.randomUUID().toString();
        String otpValue = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiredTime = LocalDateTime.now().plusMinutes(5);
        String expiredTimeFormatted = expiredTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String lastRequestTimeKey = phoneNumber + "_lastRequestTime";
        String lastRequestTimeStr = redisTemplate.opsForValue().get(lastRequestTimeKey);

        String lockoutKey = phoneNumber + "_lockout";
        String lockoutTimeStr = redisTemplate.opsForValue().get(lockoutKey);
        if (lockoutTimeStr != null) {
            LocalDateTime lockoutTime = LocalDateTime.parse(lockoutTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (lockoutTime.isAfter(LocalDateTime.now())) {
                throw new GeneralException(Constants.INCORRECT_OTP_MAX);
            } else {
                redisTemplate.delete(lockoutKey);
            }
        }

        if (lastRequestTimeStr != null) {
            LocalDateTime lastRequestTime = LocalDateTime.parse(lastRequestTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (lastRequestTime.plusSeconds(60).isAfter(LocalDateTime.now())) {
                throw new GeneralException(Constants.OTP_LIMIT_GENERATE);
            }
        }

        while (Boolean.TRUE.equals(redisTemplate.hasKey(otpKey))) {
            otpKey = UUID.randomUUID().toString();
        }

        redisTemplate.opsForValue().set(otpKey, otpValue, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(otpKey + "_time", expiredTimeFormatted, 6, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(lastRequestTimeKey, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 1, TimeUnit.HOURS);

        redisTemplate.opsForValue().set(otpKey + "_phoneNumber", phoneNumber, 35, TimeUnit.MINUTES);

        CopyTradingSendOTPResponse response = new CopyTradingSendOTPResponse();
        response.setOtpKey(otpKey);
        response.setExpiredTime(expiredTimeFormatted);

        AppConf.SmsServer smsServer = appConf.getSmsServer();
        if (smsServer == null) {
            throw new InvalidValueException("domain");
        }

        sendSms(phoneNumber, otpValue, request.getAcceptLanguage(), smsServer);

        return response;
    }

    @Override
    public CopyTradingVerifyOTPResponse verifyOtp(CopyTradingVerifyOTPRequest request, RequestContext<CopyTradingVerifyOTPRequest> ctx) {

        log.info("[verifyOtp] ctxId: {}, copyTradingVerifyOTPRequest: {}", ctx.getId(), request);
        String otpKey = request.getOtpKey();
        String otpValue = request.getOtpValue();

        String phoneNumber = redisTemplate.opsForValue().get(otpKey + "_phoneNumber");
        if (phoneNumber == null) {
            throw new GeneralException(Constants.OTP_KEY_DOES_NOT_EXIST);
        }

        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        String lockoutKey = phoneNumber + "_lockout";
        String failedAttemptsKey = otpKey + "_failedAttempts";
        String failedAttemptsStr = redisTemplate.opsForValue().get(failedAttemptsKey);
        int failedAttempts = failedAttemptsStr == null ? 0 : Integer.parseInt(failedAttemptsStr);

        String expiredTimeStr = redisTemplate.opsForValue().get(otpKey + "_time");

        if (storedOtp == null) {
            throw new GeneralException(Constants.OTP_KEY_DOES_NOT_EXIST);
        }
        String lockoutTimeStr = redisTemplate.opsForValue().get(lockoutKey);
        if (lockoutTimeStr != null) {
            LocalDateTime lockoutTime = LocalDateTime.parse(lockoutTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (lockoutTime.isAfter(LocalDateTime.now())) {
                throw new GeneralException(Constants.INCORRECT_OTP_MAX);
            } else {
                redisTemplate.delete(lockoutKey);
            }
        }

        if (!storedOtp.equals(otpValue)) {
            failedAttempts++;
            redisTemplate.opsForValue().set(failedAttemptsKey, String.valueOf(failedAttempts), 30, TimeUnit.MINUTES);
            if (failedAttempts > 3) {
                redisTemplate.opsForValue().set(lockoutKey, LocalDateTime.now().plusMinutes(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 30, TimeUnit.MINUTES);
                throw new GeneralException(Constants.INCORRECT_OTP_MAX);
            }
            throw new GeneralException(Constants.INCORRECT_OTP);
        }

        LocalDateTime expiredTime = LocalDateTime.parse(expiredTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (expiredTime.isBefore(LocalDateTime.now())) {
            throw new GeneralException(Constants.OTP_EXPIRED);
        }

        redisTemplate.delete(failedAttemptsKey);

        CopyTradingVerifyOTPResponse response = new CopyTradingVerifyOTPResponse();
        response.setOtpKey(otpKey);
        response.setExpiredTime(expiredTimeStr);

        return response;
    }

    private void sendSms(String phoneNumber, String otpValue, String acceptLanguage, AppConf.SmsServer smsServer) {
        String message = "";
        if ("vi".equals(acceptLanguage)) {
            message = otpMessagePrefixVI + otpValue + otpMessageSuffixVI;
        } else if ("en".equals(acceptLanguage)) {
            message = otpMessagePrefixEN + otpValue + otpMessageSuffixEN;
        }
        SendMT body = new SendMT();
        body.setUser(smsServer.getUser());
        body.setPass(smsServer.getPass());
        body.setSms(message);
        body.setSenderName(smsServer.getSenderName());
        body.setPhone(phoneNumber);
        body.setIsFlash(smsServer.getIsFlash());
        body.setIsUnicode(smsServer.getIsUnicode());
        this.soapClient.setDefaultUri(smsServer.getUrl());
        SendMTResponse response = (SendMTResponse) this.soapClient.callWebService(smsServer.getUrl(), body, new SoapActionCallback(smsServer.getSoapAction()));
        log.info("Send SMS response: {}", response.getSendMTResult());
    }

    public String getPhoneNumber(String acntNo) {

        String url = rootURL + "/tsol/apikey/tuxsvc/account/user/get-account-information";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("apiKey", apiKey);

        String requestBody = "{\"acnt_no\":\"" + acntNo + "\"}";
        log.info("Request Body: {}", requestBody);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<CopyTradingSendOTPGetPhoneResponse> response =
            REST_TEMPLATE.exchange(url, HttpMethod.POST, entity, CopyTradingSendOTPGetPhoneResponse.class);
        log.info("Response Status Code: {}", response.getStatusCode());

        if (response.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        CopyTradingSendOTPGetPhoneResponse responseBody = response.getBody();

        if (responseBody != null && responseBody.getDataList() != null && !responseBody.getDataList().isEmpty()) {
            log.info("Response body is not null, data list is not empty. Retrieving phone number...");
            String phoneNumber = responseBody.getDataList().get(0).getPhone();
            log.info("Phone number retrieved: {}", phoneNumber);
            return phoneNumber;
        }
        return null;
    }
}
