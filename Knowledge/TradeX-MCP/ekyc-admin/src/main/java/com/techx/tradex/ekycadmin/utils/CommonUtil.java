package com.techx.tradex.ekycadmin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techx.tradex.ekycadmin.constant.Constants;
import com.techx.tradex.ekycadmin.constant.Messages;
import com.techx.tradex.ekycadmin.constant.Messages;
import java.util.Objects;
import com.techx.tradex.ekycadmin.constant.Constants;
import com.techx.tradex.ekycadmin.constant.Messages;
import com.techx.tradex.ekycadmin.models.dto.JwtTraDexDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.util.Strings;

import javax.xml.bind.ValidationException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
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
                log.error("[objectToStringJsonIgnoreError] error message: ", e);
            }
        }
        return json;
    }

    public static JwtTraDexDTO decodeTraDexJwt(String jwtToken) throws Exception {
        String[] pieces = jwtToken.split("\\.");
        String b64payload = pieces[1];
        String jsonString = new String(Base64.decodeBase64(b64payload), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        return new ObjectMapper().readValue(jsonString, JwtTraDexDTO.class);
    }

    public static String extractToken(String bearerToken) throws Exception {
        if (Objects.isNull(bearerToken)) {
            throw new ValidationException(Messages.TOKEN_IS_REQUIRED);
        } else if (bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        }
        throw new ValidationException(MessageFormat.format(Messages.TOKEN_IS_INVALID, bearerToken));
    }
}
