package com.difisoft.nhsv.admin.service.vietstock.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CustomDateDeserializer for handling date value in the format "/Date(timestamp)/" from VietStock API
 * Example: "/Date(1736096400000)/" to 1736096400000
 */
@Slf4j
public class CustomVietStockDateDeserializer extends JsonDeserializer<Long> {
    private static final Pattern DATE_PATTERN = Pattern.compile("/Date\\((\\d+)\\)/");

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String dateStr = jsonParser.getText();
        if (null == dateStr || dateStr.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = DATE_PATTERN.matcher(dateStr.trim());
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        log.error("Cannot parse date: {}", dateStr);
        return null;
    }
}
