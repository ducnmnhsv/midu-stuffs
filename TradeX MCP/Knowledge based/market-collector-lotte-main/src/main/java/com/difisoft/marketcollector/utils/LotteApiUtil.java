package com.difisoft.marketcollector.utils;

import com.difisoft.marketcollector.configurations.AppConf;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class LotteApiUtil {
    public static <T> T get(
            String logId,
            String uri,
            Class<T> clazz,
            Object optionalBody,
            ObjectMapper objectMapper,
            AppConf.ApiConnection apiConfig,
            Logger log
    ) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            RequestBuilder builder = RequestBuilder.
                    get(apiConfig.getBaseUrl() + uri).
                    setHeader("Content-Type", "application/json").
                    setHeader("apiKey", apiConfig.getApiKey());
            AtomicReference<String> bodyString = new AtomicReference<>();
            if (optionalBody != null) {
                bodyString.set(objectMapper.writeValueAsString(optionalBody));
                builder.setEntity(new StringEntity(bodyString.get()));
            }

            HttpUriRequest request = builder.build();


            return client.execute(request, httpResponse -> {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                log.info("{} request {}-{} and response {} with status code {}", logId, request.getURI(), bodyString.get(), httpResponse, statusCode);
                if (statusCode != 200) {
                    throw new RuntimeException("Request failed with status code: " + statusCode);
                }
                String response = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                log.info("{} query {} with data {}", logId, uri, response);
                return objectMapper.readValue(response, clazz);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
