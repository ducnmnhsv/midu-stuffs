package com.techx.tradex.ekycadmin.service.impl;

import com.techx.tradex.ekycadmin.constant.Constants;
import com.techx.tradex.ekycadmin.models.request.FptECEnvelopesRecipientRequest;
import com.techx.tradex.ekycadmin.models.request.FptECExCallRequest;
import com.techx.tradex.ekycadmin.models.request.FptECLoginRequest;
import com.techx.tradex.ekycadmin.models.response.FptECEnvelopesRecipientResponse;
import com.techx.tradex.ekycadmin.models.response.FptECExCallResponse;
import com.techx.tradex.ekycadmin.models.response.FptECLoginResponse;
import com.techx.tradex.ekycadmin.models.response.FptECTemplateStructureResponse;
import com.techx.tradex.ekycadmin.service.FptEContractApiClient;
import com.techx.tradex.ekycadmin.utils.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FptEContractApiClientImpl {

    private final FptEContractApiClient fptEContractApiClient;

    public FptECLoginResponse login(String prefixLog, @Valid FptECLoginRequest request) {
        try {
            log.info("{} -- login -- request: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(request));
            ResponseEntity<FptECLoginResponse> response = this.fptEContractApiClient.login(request);
            log.info("{} -- login -- response: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(response));
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("{} -- login -- error: ", prefixLog, e);
        }
        return null;
    }

    public FptECTemplateStructureResponse getTemplateStructure(String prefixLog, String alias, String token) {
        try {
            log.info("{} -- getTemplateStructure -- request -- alias: {}, token: {}", prefixLog, alias, token);
            ResponseEntity<FptECTemplateStructureResponse> response =
                    this.fptEContractApiClient.getTemplateStructure(Constants.TOKEN_PREFIX + token, alias);
            log.info("{} -- getTemplateStructure -- response: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(response));
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("{} -- getTemplateStructure -- error: ", prefixLog, e);
        }
        return null;
    }

    public FptECExCallResponse exCall(String prefixLog, FptECExCallRequest request, String token) {
        try {
            log.info("{} -- exCall -- token: {}, request: {}", prefixLog, token, request.toString());
            ResponseEntity<FptECExCallResponse> response = this.fptEContractApiClient.exCall(Constants.TOKEN_PREFIX + token, request);
            log.info(
                    "{} -- exCall -- response: {}"
                    , prefixLog
                    , Objects.nonNull(response) && Objects.nonNull(response.getBody())
                            ? response.getBody().toString()
                            : CommonUtil.objectToStringJsonIgnoreError(response)
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("{} -- exCall -- error: ", prefixLog, e);
        }
        return null;
    }

    public List<FptECEnvelopesRecipientResponse> envelopesRecipient(
            String prefixLog
            , String token
            , Integer page
            , Integer size
            , Boolean checkAuthenticate
            , FptECEnvelopesRecipientRequest request
    ) {
        try {
            log.info("{} -- envelopesRecipient -- token: {}, request: {}", prefixLog, token, request);
            ResponseEntity<List<FptECEnvelopesRecipientResponse>> response = this.fptEContractApiClient.envelopesRecipient(
                    Constants.TOKEN_PREFIX + token
                    , page
                    , size
                    , checkAuthenticate
                    , request
            );
            log.info("{} -- envelopesRecipient -- response: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(response));
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("{} -- envelopesRecipient -- error: ", prefixLog, e);
        }
        return null;
    }
}
