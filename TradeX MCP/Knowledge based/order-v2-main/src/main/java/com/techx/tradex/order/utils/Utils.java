package com.techx.tradex.order.utils;

import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.DefaultUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techx.tradex.order.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Slf4j
public class Utils {
    public static ZonedDateTime getCurrentMarketDate() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(DefaultUtils.UTC_ID);
        return zonedDateTime.truncatedTo(ChronoUnit.DAYS);
    }

    public static Date getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, Constants.MARKET_TIMEZONE);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public static <T extends DataRequest> String getRawUserName(T request) {
        if (request.getHeaders() != null) {
            if (request.getHeaders().getToken() != null) {
                if (request.getHeaders().getToken().getUserData() != null) {
                    if (request.getHeaders().getToken().getUserData().getUsername() != null) {
                        return request.getHeaders().getToken().getUserData().getUsername();
                    }
                }
            }
        }
        return null;
    }

    public static String objectToStringJsonIgnoreError(Object obj) {
        ObjectMapper ow = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = Strings.EMPTY;
        if (Objects.nonNull(obj)) {
            try {
                json = ow.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                log.error("[objectToStringJsonIgnoreError] error message: ", e);
            }
        }
        return json;
    }
}
