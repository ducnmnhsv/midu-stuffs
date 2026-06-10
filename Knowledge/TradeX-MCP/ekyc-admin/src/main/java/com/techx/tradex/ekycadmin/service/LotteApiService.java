package com.techx.tradex.ekycadmin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.constants.ErrorCodeEnums;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.exceptions.InvalidParameterException;
import com.techx.tradex.common.utils.Pair;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.models.lotte.LotteRes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class LotteApiService {

    private static final Logger log = LoggerFactory.getLogger(LotteApiService.class);

    private final ObjectMapper objectMapper;
    private final AppConf appConf;
    private static final String LOTTE_INVALID_PREFIX = "INVALID_INPUT_PARAMETER.";

    public LotteApiService(ObjectMapper objectMapper, AppConf appConf) {
        this.objectMapper = objectMapper;
        this.appConf = appConf;
    }

    public <T extends LotteRes> Pair<T, String> postData(URI uri, Class<T> clazz, Object bodyData, String txId) throws IOException {
        try {
            return this.post(uri, clazz, bodyData, txId);
        } catch (Exception e) {
            log.error("{} Error while calling Lotte API", txId, e);
            throw e;
        }
    }

    public  <T> Pair<T, String> postFormData(URI uri, Class<T> clazz, HttpEntity bodyFormData, String txId) throws Exception {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(uri);
            request.setHeader("apiKey", appConf.getLotteConfig().getApiKey());
            request.setHeader("cache-control", "no-cache");
            request.setHeader("Content-Type", bodyFormData.getContentType().getValue());
            request.setEntity(bodyFormData);
            long time = System.currentTimeMillis();
            return client.execute(request, httpResponse -> {
                String response = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                request.getEntity().writeTo(stream);
                log.warn("{} query --{}-- with headers --{}-- with form data --{}-- and response body --{}-- and status --{}-- took {} ms",
                    txId, uri.toString(), request.getAllHeaders(), stream.toString().substring(0, 800), response, httpResponse.getStatusLine().getStatusCode(), System.currentTimeMillis() - time);
                return new Pair<T, String>(objectMapper.readValue(response, clazz), response);
            });
        }
    }

    public <T> Pair<T, String> post(URI uri, Class<T> clazz, Object bodyData, String txId) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(uri);
            String body = objectMapper.writeValueAsString(bodyData);
            request.setHeader("apiKey", appConf.getLotteConfig().getApiKey());
            request.setHeader("cache-control", "no-cache");
            request.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
            request.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            long time = System.currentTimeMillis();
            return client.execute(
                request,
                httpResponse -> {
                    String response = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                    log.warn(
                        "{} query --{}-- with headers --{}-- with data --{}-- and response body --{}-- and status --{}-- took {} ms",
                        txId,
                        uri.toString(),
                        request.getAllHeaders(),
                        body,
                        response,
                        httpResponse.getStatusLine().getStatusCode(),
                        System.currentTimeMillis() - time
                    );
                    return new Pair<T, String>(objectMapper.readValue(response, clazz), response);
                }
            );
        }
    }

    public <T extends LotteRes> Pair<T, String> getData(URI uri, Class<T> clazz, Object bodyData, String txId) throws IOException {
        try {
            return this.get(uri, clazz, bodyData, txId);
        } catch (Exception e) {
            log.error("{} Error while calling Lotte API", txId, e);
            throw e;
        }
    }

    public <T> Pair<T, String> get(URI uri, Class<T> clazz, Object bodyData, String txId) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGetWithEntity request = new HttpGetWithEntity(uri);
            String body = objectMapper.writeValueAsString(bodyData);
            request.setHeader("apiKey", appConf.getLotteConfig().getApiKey());
            request.setHeader("cache-control", "no-cache");
            request.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
            request.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            if (body != null) {
                request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            }
            long time = System.currentTimeMillis();
            return client.execute(
                request,
                httpResponse -> {
                    String response = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                    log.warn(
                        "{} query --{}-- with headers --{}-- with data --{}-- and response body --{}-- and status --{}-- took {} ms",
                        txId,
                        uri.toString(),
                        request.getAllHeaders(),
                        body,
                        response,
                        httpResponse.getStatusLine().getStatusCode(),
                        System.currentTimeMillis() - time
                    );
                    return new Pair<T, String>(objectMapper.readValue(response, clazz), response);
                }
            );
        }
    }

    public UriComponentsBuilder getUrl(String url) {
        return UriComponentsBuilder.fromUriString(url.replace("#{rootUrl}", appConf.getLotteConfig().getRootUrl()));
    }

    public class HttpGetWithEntity extends HttpPost {

        public static final String METHOD_NAME = "GET";

        public HttpGetWithEntity(URI url) {
            super(url);
        }

        public HttpGetWithEntity(String url) {
            super(url);
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }

    public static Pair<String, String> parseMessages(String errorDesc, String errorCode, AppConf appConf) {
        String codes = null;
        String messages = null;
        if (!errorDesc.isEmpty()) {
            int startIndex = errorDesc.indexOf('[');
            int endIndex = errorDesc.indexOf(']');
            if (startIndex >= 0 && endIndex > 0) {
                codes = errorDesc.substring(startIndex + 2, endIndex);
                messages = errorDesc.substring(endIndex + 1);
            }
            if (codes == null) {
                codes = "INTERNAL_SERVER_ERROR";
            }
        }
        if (appConf.getLotteConfig().getResponseErrorCodeBusiness().contains(errorCode) && !Objects.equals(codes, "2016")) {
            if (errorDesc.startsWith(LOTTE_INVALID_PREFIX)) {
                messages = errorDesc.substring(LOTTE_INVALID_PREFIX.length() + 1);
                throw new GeneralException(ErrorCodeEnums.INVALID_PARAMETER.name(), messages);
            }
            throw new GeneralException(messages);
        }
        return new Pair<String, String>(codes, messages);
    }
}
