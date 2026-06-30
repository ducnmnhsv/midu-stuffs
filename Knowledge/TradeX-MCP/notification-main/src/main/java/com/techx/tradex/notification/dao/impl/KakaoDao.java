package com.techx.tradex.notification.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.model.KakaoMessageRequest;
import com.techx.tradex.notification.model.KakaoMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Repository
public class KakaoDao implements com.techx.tradex.notification.dao.KakaoDao {

    private AppConf appConf;
    ObjectMapper objectMapper;
    RestTemplate restTemplate;

    @Autowired
    public KakaoDao(AppConf appConf, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.appConf = appConf;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public KakaoMessageResponse sendMessage(KakaoMessageRequest request) {
        final LinkedMultiValueMap<String, String> hd = new LinkedMultiValueMap<>();
        hd.add("Content-Type", "application/json");
        HttpEntity<KakaoMessageRequest> requestEntity = new HttpEntity<>(request, hd);
        ResponseEntity<KakaoMessageResponse> response = restTemplate.exchange(
                appConf.getKakao().getUrl(),
                HttpMethod.POST,
                requestEntity,
                KakaoMessageResponse.class
        );
        return response.getBody();
    }
}
