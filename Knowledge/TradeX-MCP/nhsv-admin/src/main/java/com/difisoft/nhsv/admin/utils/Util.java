package com.difisoft.nhsv.admin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.Objects;

@Slf4j
public class Util {

    public static boolean isNotEmptyBlank(String str) {
        return StringUtils.isNotEmpty(str) && StringUtils.isNotBlank(str);
    }

    public static String objectToStringJsonIgnoreError(Object obj) {
        String json = Strings.EMPTY;
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        if (Objects.nonNull(obj)) {
            try {
                json = objectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                log.error("[objectToStringJsonIgnoreError] error message: ", e);
            }
        }
        return json;
    }
}
