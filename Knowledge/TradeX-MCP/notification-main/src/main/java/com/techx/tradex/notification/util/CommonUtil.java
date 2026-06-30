package com.techx.tradex.notification.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.Objects;

@Slf4j
public class CommonUtil {
    public static String objectToStringJsonIgnoreError(Object obj) {
        ObjectMapper ow = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = Strings.EMPTY;
        if (Objects.nonNull(obj)) {
            try {
                json = ow.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                log.error("[objectToStringJsonIgnoreError] error message: {}, cause: {}", e.getMessage(), e.getCause().toString());
            }
        }
        return json;
    }
}
