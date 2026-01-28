package com.techx.tradex.order.model.converters;

import com.difisoft.model.requests.Headers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.services.CacheService;
import jakarta.persistence.AttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderConvert implements AttributeConverter<Headers, String> {
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    private static ObjectMapper om = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Headers attribute) {
        String json = null;
        try {
            json = om.writeValueAsString(attribute);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }
        return json;
    }

    @Override
    public Headers convertToEntityAttribute(String dbData) {
        Headers headers = null;
        try {
            headers = om.readValue(dbData, Headers.class);
        } catch (JsonProcessingException e) {
            log.error("JSON reading error", e);
        }
        return headers;
    }
}
