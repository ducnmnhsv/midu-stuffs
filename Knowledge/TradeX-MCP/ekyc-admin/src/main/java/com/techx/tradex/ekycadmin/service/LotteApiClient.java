package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.models.request.LotteAccountNumberRequest;
import com.techx.tradex.ekycadmin.models.response.LotteAccountNumberResponse;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${app.feignClient.lotteApi.name}", url = "${app.feignClient.lotteApi.host}")
public interface LotteApiClient {
    @GetMapping(
        value = "/tuxsvc/ekyc/stk-acc-info",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: application/json")
    ResponseEntity<LotteAccountNumberResponse> getAccountNumberInfo(
        @RequestHeader("apiKey") String apiKey,
        @RequestBody LotteAccountNumberRequest request
    );
}
