package com.techx.tradex.ekycadmin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.constant.Constants;
import com.techx.tradex.ekycadmin.models.enums.OtpIdType;
import com.techx.tradex.ekycadmin.models.enums.OtpTxType;
import com.techx.tradex.ekycadmin.models.redis.Otp;
import com.techx.tradex.ekycadmin.models.redis.OtpValidation;
import com.techx.tradex.ekycadmin.models.request.InitSmsOtpRequest;
import com.techx.tradex.ekycadmin.models.request.SendOtpRequest;
import com.techx.tradex.ekycadmin.models.request.VerifyOtpRequest;
import com.techx.tradex.ekycadmin.models.response.SendOtpResponse;
import com.techx.tradex.ekycadmin.models.response.VerifyOtpResponse;
import com.techx.tradex.ekycadmin.utils.Util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OTPService {

    private static final Logger log = LoggerFactory.getLogger(OTPService.class);

    private final CacheService cacheService;
    private final AppConf appConf;
    private RequestSender requestSender;
    private ObjectMapper objectMapper;

    @Autowired
    public OTPService(CacheService cacheService, AppConf appConf, RequestSender requestSender, ObjectMapper objectMapper) {
        this.cacheService = cacheService;
        this.appConf = appConf;
        this.requestSender = requestSender;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<SendOtpResponse> generateAndSendOtp(SendOtpRequest request) throws IOException {
        request.validate();
        String otp = this.generateOtp(appConf.getOtpLength());
        String otpId = UUID.randomUUID().toString();
        Map<String, Long> LifeTime = request.getIdType().equals(OtpIdType.EMAIL.name())
            ? appConf.getOtpLifeTime().getEmail()
            : appConf.getOtpLifeTime().getSms();
        Long otpLifeTime = LifeTime.get(request.getTxType());
        String username = request.getTxType().equals(OtpTxType.REGISTER.name()) || request.getTxType().equals(OtpTxType.E_KYC.name())
            ? request.getId()
            : request.getHeaders().getToken().getServiceUsername();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredTime = now.plusSeconds(otpLifeTime);
        try {
            OtpValidation otpValidation = this.cacheService.findOtpValidation(username);
            if (otpValidation.getLatestRequest().plusSeconds(appConf.getOtpGenTime()).isAfter(now)) {
                throw new GeneralException(Constants.OTP_GENERATE_TO_FAST);
            }
            otpValidation.setCount(otpValidation.getCount() + 1);
            if (otpValidation.getCount() > appConf.getOtpMaxGenTime()) {
                throw new GeneralException(Constants.OTP_LIMIT_GENERATE);
            }
            if (appConf.getHandlerVerifyOtpFail() && otpValidation.getFailedCount() >= appConf.getOptMaxVerifyFail()) {
                throw new GeneralException(Constants.PHONENO_LOCK_INCORRECT_OTP_MAX);
            }
            otpValidation.setLatestRequest(now);
            this.cacheService.addOtpValidation(otpValidation);
        } catch (Exception ex) {
            if (!ex.getMessage().equals(Constants.OBJECT_NOT_FOUND)) {
                throw ex;
            }
            OtpValidation otpValidation = new OtpValidation(username, 0, 0, now);
            this.cacheService.addOtpValidation(otpValidation);
        }
        Otp verification = new Otp(request.getId(), otp, request.getTxType(), request.getIdType(), username);
        InitSmsOtpRequest initSmsOtpRequest = new InitSmsOtpRequest();
        initSmsOtpRequest.setMethod(Constants.TOPIC.get(request.getIdType()));
        initSmsOtpRequest.setDomain(request.getHeaders().getToken().getDomain());
        initSmsOtpRequest.setLocale(request.getHeaders() == null ? null : request.getHeaders().getAcceptLanguage());
        if (request.getHeaders().getToken().getDomain().equals("nhsv") && request.getIdType().equals(OtpIdType.PHONE_NO.name())) {
            if (request.getId().startsWith("0")) {
                request.setId(String.format("84%s", request.getId().substring(1)));
            }
            if (request.getId().startsWith("+")) {
                request.setId(request.getId().substring(1));
            }
        }
        initSmsOtpRequest = initSmsOtpRequest.toInitSmsOtpRequest(initSmsOtpRequest, otp, request.getId(), objectMapper);
        this.requestSender.sendMessage(appConf.getTopics().getNotification(), "", initSmsOtpRequest);
        this.cacheService.addOtp(verification, otpId, otpLifeTime);
        log.warn("contact {} - otp generated: {}", request.getId(), otp);
        return CompletableFuture.completedFuture(new SendOtpResponse(otpId, Util.toDateTimeFormat(expiredTime)));
    }

    public CompletableFuture<VerifyOtpResponse> VerifyOtp(VerifyOtpRequest request) throws IOException {
        request.validate();
        Otp verification = null;
        try {
            verification = this.cacheService.findOtp(request.getOtpId());
        } catch (Exception ex) {
            if (ex.getMessage().equals(Constants.OBJECT_NOT_FOUND)) {
                throw new GeneralException("OTP_EXPIRED");
            } else {
                throw ex;
            }
        }
        if (verification == null) {
            throw new GeneralException(Constants.ID_NOT_FOUND);
        }
        OtpValidation otpValidation;
        try {
            otpValidation = this.cacheService.findOtpValidation(verification.getUsername());
        }
        catch (Exception e) {
            if (!e.getMessage().equals(Constants.OBJECT_NOT_FOUND)) {
                throw e;
            }
            otpValidation = new OtpValidation(verification.getUsername(), 1, 0, LocalDateTime.now());
        }
        try {
            if (!verification.getOtp().equals(request.getOtpValue())) {
                otpValidation.setFailedCount(otpValidation.getFailedCount() + 1);
                if (appConf.getHandlerVerifyOtpFail() && otpValidation.getFailedCount() >= appConf.getOptMaxVerifyFail()) {
                    throw new GeneralException(Constants.INCORRECT_OTP_MAX);
                }
                throw new GeneralException(Constants.INCORRECT_OTP, Collections.singletonList(otpValidation.getFailedCount().toString()));
            } else {
                otpValidation.setFailedCount(0);
            }
        }
        finally {
            this.cacheService.addOtpValidation(otpValidation);
        }
        String otpKey = UUID.randomUUID().toString();
        this.cacheService.addOtpKey(verification, otpKey, appConf.getOtpKeyLifeTime());
        this.cacheService.removeVerifiedOtp(request.getOtpId());
        LocalDateTime expiredTime = LocalDateTime.now().plusSeconds(appConf.getOtpKeyLifeTime());
        return CompletableFuture.completedFuture(new VerifyOtpResponse(otpKey, Util.toDateTimeFormat(expiredTime)));
    }

    public void verifyOtpKey(String otpKey, String username, String otpTxType) throws JsonProcessingException {
        Otp verification = this.cacheService.findOtpKey(otpKey);
        if (verification == null) {
            throw new GeneralException(Constants.INVALID_OTP_KEY);
        }
        if (!verification.getUsername().equals(username)) {
            throw new GeneralException(Constants.OTP_WRONG_USER);
        }
        if (!verification.getOtpTxType().equals(otpTxType)) {
            throw new GeneralException(Constants.OTP_WRONG_TYPE);
        }
        this.cacheService.removeVerifiedOtpKey(otpKey);
    }

    private String generateOtp(Integer codeLength) {
        StringBuilder verificationCode = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            verificationCode.append(Math.round(Math.random() * 9));
        }
        return verificationCode.toString();
    }
}
