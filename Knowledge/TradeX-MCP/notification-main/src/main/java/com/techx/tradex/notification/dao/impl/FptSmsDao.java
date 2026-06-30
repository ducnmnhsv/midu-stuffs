package com.techx.tradex.notification.dao.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.model.SmsMessageRequest;
import com.techx.tradex.notification.model.db.FptSmsHistory;
import com.techx.tradex.notification.repository.FptSmsHistoryRepository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Error {
    private String error;
    private String errorDescription;
}

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class AuthRequest {
    private String grantType;
    private String clientId;
    private String clientSecret;
    private String scope;
    private String sessionId;
}


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class AuthResponse {
    private String accessToken;
    private Long expiresIn;
    private String tokenType;
    private String scope;
}

@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
class SendSmsRequest {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("session_id")
    private String sessionId;
    private String brandName;
    private String phone;
    private String message;
}

@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
class SendSmsResponse {
    private String messageId;
    private String phone;
    private String brandName;
    private String message;
    private String partnerId;
    private String telco;
    private Integer isSent;
}

@Repository
public class FptSmsDao {
    private static final Logger log = LoggerFactory.getLogger(FptSmsDao.class);

    private RestTemplate restTemplate;
    private AppConf appConf;
    private FptSmsHistoryRepository fptSmsHistoryRepository;

    private String sessionId = UUID.randomUUID().toString();
    private long sessionIdNumber;
    private String accessToken;
    private Long expiredAt;

    @Autowired
    public FptSmsDao(
            AppConf appConf,
            RestTemplate restTemplate,
            FptSmsHistoryRepository fptSmsHistoryRepository
    ) {
        this.appConf = appConf;
        this.restTemplate = restTemplate;
        this.fptSmsHistoryRepository = fptSmsHistoryRepository;
    }

    public void sendSms(SmsMessageRequest request) {
        this.sendSms(request, 0);
    }

    private String toDateTimeFormat(ZonedDateTime time) {
        return DateTimeFormatter.ofPattern("yyyyMMdd").format(time);
    }

    private void sendSms(SmsMessageRequest request, int index) {
        ZonedDateTime now = ZonedDateTime.now();
        int length = request.getContent().length();
        FptSmsHistory fptSmsHistory = new FptSmsHistory();
        fptSmsHistory.setBrand_name(appConf.getFptSms().getBrandName());
        fptSmsHistory.setPhone_number(request.getPhoneNumber());
        fptSmsHistory.setContent(request.getContent());
        fptSmsHistory.setMessage_type("CSKH");
        fptSmsHistory.setCreated_at(now);
        fptSmsHistory.setDate(toDateTimeFormat(now.plusHours(7)));
        fptSmsHistory.setUpdated_at(now);
        fptSmsHistory.setStatus("CREATED");
        fptSmsHistory.setQty(length <= 160 ? 1 : length <= 306 ? 2 : length <= 459 ? 3 : 4);
        this.fptSmsHistoryRepository.save(fptSmsHistory);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (this.accessToken == null || System.currentTimeMillis() > this.expiredAt) {
            this.accessToken = null;

            fptSmsHistory.setStatus("EXCHANGE_TOKEN");
            this.fptSmsHistoryRepository.save(fptSmsHistory);
            AuthRequest authRequest = new AuthRequest();
            authRequest.setClientId(appConf.getFptSms().getClientId());
            authRequest.setClientSecret(appConf.getFptSms().getClientSecret());
            authRequest.setGrantType("client_credentials");
            authRequest.setScope(appConf.getFptSms().getScope());
            String sessionId = this.getSessionId();
            authRequest.setSessionId(sessionId);
            HttpEntity<AuthRequest> authEntity = new HttpEntity<>(authRequest, headers);

            try {
                ResponseEntity<AuthResponse> response = restTemplate.exchange(
                        appConf.getFptSms().getAuthUrl(),
                        HttpMethod.POST,
                        authEntity,
                        AuthResponse.class
                );
                this.accessToken = response.getBody().getAccessToken();
                this.expiredAt = System.currentTimeMillis() + response.getBody().getExpiresIn() - 120;
            } catch (HttpStatusCodeException e) {
                log.error("{} fail to exchange for token response: {} {}", sessionId, e.getResponseHeaders(), e.getResponseBodyAsString());
                fptSmsHistory.setStatus("TOKEN_FAILED");
                fptSmsHistory.setFail_reason(e.getResponseBodyAsString());
                this.fptSmsHistoryRepository.save(fptSmsHistory);
                throw e;
            }
        }
        fptSmsHistory.setStatus("REQUEST_SENDING");
        this.fptSmsHistoryRepository.save(fptSmsHistory);
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        sendSmsRequest.setAccessToken(this.accessToken);
        sendSmsRequest.setBrandName(appConf.getFptSms().getBrandName());
        sendSmsRequest.setPhone(request.getPhoneNumber());
        sendSmsRequest.setMessage(Base64Utils.encodeToString(request.getContent().getBytes()));
        String sessionId = this.getSessionId();
        sendSmsRequest.setSessionId(sessionId);
        HttpEntity<SendSmsRequest> smsEntity = new HttpEntity<>(sendSmsRequest, headers);
        try {
            ResponseEntity<SendSmsResponse> smsResponse = restTemplate.exchange(
                    appConf.getFptSms().getSendUrl(),
                    HttpMethod.POST,
                    smsEntity,
                    SendSmsResponse.class
            );
            log.info("{} successfully send to sms {}", sessionId, smsResponse.getBody());
            fptSmsHistory.setStatus("SUCCESS");
            if (smsResponse.getBody() != null) {
                fptSmsHistory.setTelco(smsResponse.getBody().getTelco());
            }
            this.fptSmsHistoryRepository.save(fptSmsHistory);
        } catch (HttpStatusCodeException e) {
            fptSmsHistory.setStatus("FAILED");
            fptSmsHistory.setFail_reason(e.getResponseBodyAsString());
            this.fptSmsHistoryRepository.save(fptSmsHistory);
            HttpStatus status = e.getStatusCode();
            if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                if (index == 0) {
                    log.warn("{} seem token is expired. will retry", sessionId, request, e.getResponseBodyAsString());
                    this.accessToken = null;
                    this.sendSms(request, index + 1);
                } else {
                    log.error("{} fail to send sms due to unauthtorized of forbidden (after retried) to {} with response {}, {}", sessionId, request, e.getResponseBodyAsString(), status.value());
                    throw e;
                }
            } else {
                log.error("{} fail to send sms to {} with response {}", sessionId, request, e.getResponseBodyAsString());
                throw e;
            }
        }
    }

    private String getSessionId() {
        this.sessionIdNumber++;
        return this.sessionId + "-" + this.sessionIdNumber;
    }
}
