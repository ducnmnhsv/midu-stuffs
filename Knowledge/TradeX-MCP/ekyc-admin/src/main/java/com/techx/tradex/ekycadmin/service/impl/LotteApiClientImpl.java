package com.techx.tradex.ekycadmin.service.impl;

import com.techx.tradex.ekycadmin.models.request.LotteAccountNumberRequest;
import com.techx.tradex.ekycadmin.models.response.LotteAccountNumberResponse;
import com.techx.tradex.ekycadmin.service.LotteApiClient;
import com.techx.tradex.ekycadmin.utils.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LotteApiClientImpl {

    private final LotteApiClient lotteApiClient;

    public LotteAccountNumberResponse getAccountNumberInfo(String prefixLog, LotteAccountNumberRequest request, String apiKey) {
        try {
            log.info("{} -- getAccountNumberInfo -- accessToken: {}, request: {}", prefixLog, apiKey, CommonUtil.objectToStringJsonIgnoreError(request));
            ResponseEntity<LotteAccountNumberResponse> response = this.lotteApiClient.getAccountNumberInfo(apiKey, request);
            log.info("{} -- getAccountNumberInfo -- response: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(response));
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("{} -- getAccountNumberInfo -- error: ", prefixLog, e);
        }
        return null;
    }
}
