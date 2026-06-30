package com.techx.tradex.notification.dao.impl;

import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.model.SmsMessageRequest;
import com.techx.tradex.notification.model.wsdl.SendMT;
import com.techx.tradex.notification.model.wsdl.SendMTResponse;
import com.techx.tradex.notification.services.SoapClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.ws.soap.client.core.SoapActionCallback;

@Repository
public class SmsDao implements com.techx.tradex.notification.dao.SmsDao {
    private static final Logger log = LoggerFactory.getLogger(SmsDao.class);

    private RestTemplate restTemplate;
    private FptSmsDao fptSmsDao;
    private SoapClient soapClient;

    @Autowired
    public SmsDao(
            RestTemplate restTemplate,
            FptSmsDao fptSmsDao,
            SoapClient oneSmsSoapClient
    ) {
        this.restTemplate = restTemplate;
        this.fptSmsDao = fptSmsDao;
        this.soapClient = oneSmsSoapClient;
    }

    @Override
    public void sendSms(SmsMessageRequest request, AppConf.SmsServer smsServer) {
        if (StringUtils.equalsIgnoreCase(smsServer.getDomain(), "vcsc")) {
            sendSmsVCSC(request, smsServer);
        } else if (StringUtils.equalsIgnoreCase(smsServer.getDomain(), "kis")) {
            sendSmsKIS(request, smsServer);
        } else if ("FPT_SMS".equalsIgnoreCase(smsServer.getType())) {
            this.fptSmsDao.sendSms(request);
        } else if ("ONE_SMS".equalsIgnoreCase(smsServer.getType())) {
            this.sendOneSms(request, smsServer);
        }
    }

    private void sendSmsVCSC(SmsMessageRequest request, AppConf.SmsServer smsServer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("API_KEY", "HU3uzCaqR0f0zJZ");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("Mobile", request.getPhoneNumber());
        map.add("Message", request.getContent());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                smsServer.getUrl(),
                HttpMethod.POST,
                entity,
                Object.class
        );
        log.info("Send SMS response: {}", response);
    }

    private void sendSmsKIS(SmsMessageRequest request, AppConf.SmsServer smsServer) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        UriComponents builder = UriComponentsBuilder.fromHttpUrl(smsServer.getUrl())
                .queryParam("to", request.getPhoneNumber())
                .queryParam("message", request.getContent()).build();
        log.info(builder.toUriString());
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        log.info("Send SMS response: {}", response);
    }

    private void sendOneSms(SmsMessageRequest request, AppConf.SmsServer smsServer) {
        SendMT body = new SendMT();
        body.setUser(smsServer.getUser());
        body.setPass(smsServer.getPass());
        body.setSms(request.getContent());
        body.setSenderName(smsServer.getSenderName());
        body.setPhone(request.getPhoneNumber());
        body.setIsFlash(smsServer.getIsFlash());
        body.setIsUnicode(smsServer.getIsUnicode());
        this.soapClient.setDefaultUri(smsServer.getUrl());
        SendMTResponse response = (SendMTResponse) this.soapClient.callWebService(smsServer.getUrl(), body, new SoapActionCallback(smsServer.getSoapAction()));
        log.info("Send SMS response: {}", response.getSendMTResult());
    }
}
