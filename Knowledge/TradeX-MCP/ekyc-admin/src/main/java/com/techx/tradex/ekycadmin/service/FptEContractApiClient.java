package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.models.request.FptECEnvelopesRecipientRequest;
import com.techx.tradex.ekycadmin.models.request.FptECExCallRequest;
import com.techx.tradex.ekycadmin.models.request.FptECLoginRequest;
import com.techx.tradex.ekycadmin.models.response.FptECEnvelopesRecipientResponse;
import com.techx.tradex.ekycadmin.models.response.FptECExCallResponse;
import com.techx.tradex.ekycadmin.models.response.FptECLoginResponse;
import com.techx.tradex.ekycadmin.models.response.FptECTemplateStructureResponse;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "${app.feignClient.fpt.eContract.name}", url = "${app.feignClient.fpt.eContract.host}")
public interface FptEContractApiClient {
    @PostMapping(value = "/v1/client-auth/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    ResponseEntity<FptECLoginResponse> login(@RequestBody FptECLoginRequest request);

    @GetMapping(
            value = "/services/envelope/api/external/v1/template/structue",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: application/json")
    ResponseEntity<FptECTemplateStructureResponse> getTemplateStructure(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam(value = "alias") String alias
    );

    @PostMapping(
            value = "/services/excall/api/excall",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: application/json")
    ResponseEntity<FptECExCallResponse> exCall(
            @RequestHeader(value = "Authorization") String token,
            @RequestBody FptECExCallRequest request
    );

    @PostMapping(
            value = "services/envelope/api/external/v1/envelopes/recipient",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: application/json")
    ResponseEntity<List<FptECEnvelopesRecipientResponse>> envelopesRecipient(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam(value = "page") Integer page,
            @RequestParam(value = "size") Integer size,
            @RequestParam(value = "checkAuthenticate") Boolean checkAuthenticate,
            @RequestBody @Valid FptECEnvelopesRecipientRequest request
    );
}
