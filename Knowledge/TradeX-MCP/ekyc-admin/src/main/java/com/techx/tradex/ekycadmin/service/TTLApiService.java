package com.techx.tradex.ekycadmin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.utils.Pair;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.models.ttl.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class TTLApiService {
    private final static Logger log = LoggerFactory.getLogger(TTLApiService.class);

    private final ObjectMapper objectMapper;
    private final AppConf appConf;

    private String operatorToken;

    @Autowired
    public TTLApiService(ObjectMapper objectMapper, AppConf appConf) {
        this.objectMapper = objectMapper.copy();
        this.objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        this.appConf = appConf;
    }

    public Pair<OpenAccountRes, String> openAccount(OpenAccountReq request) throws IOException, InterruptedException {
        return postData(appConf.getTtlConfig().getOpenAccountUrl(), OpenAccountRes.class, request, false);
    }

    public Pair<ListBankBranchResponse, String> listBankBranch(ListBankBranchRequest request) throws IOException, InterruptedException {
        return postData(appConf.getTtlConfig().getListBankBranchUrl(), ListBankBranchResponse.class, request, false);
    }

    private synchronized void doLoginOperator(int maxRetry) throws IOException, InterruptedException {
        this.realLoginOperator(maxRetry);
    }

    private void realLoginOperator(int maxRetry) throws IOException, InterruptedException {
        if (this.operatorToken != null) {
            return;
        }
        log.info("realLoginOperator: {}", maxRetry);
        try {
            OperatorLoginReq loginRequest = new OperatorLoginReq();
            loginRequest.setChannelID(appConf.getTtlConfig().getChannelId());
            loginRequest.setOperatorID(appConf.getTtlConfig().getOperatorId());
            loginRequest.setPassword(appConf.getTtlConfig().getOperatorPassword());
            Pair<OperatorLoginRes, String> response = post(appConf.getTtlConfig().getOperatorLoginUrl(), OperatorLoginRes.class, loginRequest);
            if ("OLS0000".equals(response.getLeft().getErrorCode())) {
                this.operatorToken = response.getLeft().getTokenID();
            } else {
                throw new GeneralException(response.getLeft().getErrorCode() + "-" + response.getLeft().getErrorMessage());
            }
        } catch (Exception ex) {
            log.error("error while send OperatorLoginReq: ", ex);
            Thread.sleep(500);
            if (maxRetry > 1) {
                realLoginOperator(maxRetry - 1);
            } else {
                throw ex;
            }
        }
    }

    public <T extends TTLRes> Pair<T, String> postData(String uri, Class<T> clazz, TtlOperatorReq bodyData) throws IOException, InterruptedException {
        return this.postData(uri, clazz, bodyData, false, true);
    }

    public <T extends TTLRes> Pair<T, String> postData(String uri, Class<T> clazz, TtlOperatorReq bodyData, boolean setHeader) throws IOException, InterruptedException {
        return this.postData(uri, clazz, bodyData, false, setHeader);
    }

    public <T extends TTLRes> Pair<T, String> postData(String uri, Class<T> clazz, TtlOperatorReq bodyData, boolean hasRetrieveOperator, boolean setHeader) throws IOException, InterruptedException {
        this.doLoginOperator(5);
        bodyData.setToken(operatorToken);
        bodyData.setOperatorID(appConf.getTtlConfig().getOperatorId());
        Pair<T, String> response = this.post(uri, clazz, bodyData, setHeader);
        if ("OLS0000".equals(response.getLeft().getErrorCode())) { // success
            return response;
        } else if ("OLS0012".equals(response.getLeft().getErrorCode()) || "OLS0027".equals(response.getLeft().getErrorCode())) {
            this.operatorToken = null;
            this.doLoginOperator(5);
            if (hasRetrieveOperator) {
                throw new GeneralException("cannot get operator token");
            }
            return this.postData(uri, clazz, bodyData, true);
        } else {
            throw new GeneralException(response.getLeft().getErrorCode() + ":" + response.getLeft().getErrorMessage());
        }
    }

    public <T> Pair<T, String> post(String uri, Class<T> clazz, Object bodyData) throws IOException {
        return this.post(uri, clazz, bodyData, true);
    }

    public <T> Pair<T, String> post(String uri, Class<T> clazz, Object bodyData, boolean setHeader) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String finalUri = this.getUrl(uri);
            HttpPost request = new HttpPost(finalUri);
            String body = objectMapper.writeValueAsString(bodyData);
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            if (setHeader) {
                request.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            }
            long time = System.currentTimeMillis();
            return client.execute(request, httpResponse -> {
                String response = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                log.warn("query --{}-- with headers --{}-- with data --{}-- and response body --{}-- and status --{}-- took {} ms", finalUri, request.getAllHeaders(), body, response, httpResponse.getStatusLine().getStatusCode(), System.currentTimeMillis() - time);
                return new Pair(objectMapper.readValue(response, clazz), response);
            });
        }
    }

    private String getUrl(String url) {
        return url.replace("#{eqtUrl}", appConf.getTtlConfig().getEquityBaseUrl())
                    .replace("#{derUrl}", appConf.getTtlConfig().getDerivativeBaseUrl());
    }
}
