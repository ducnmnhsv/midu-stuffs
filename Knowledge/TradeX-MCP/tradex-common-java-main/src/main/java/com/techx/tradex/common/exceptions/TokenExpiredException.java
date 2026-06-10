package com.techx.tradex.common.exceptions;

import com.techx.tradex.common.constants.ErrorCodeEnums;
import lombok.Data;

@Data
public class TokenExpiredException extends SubErrorsException {
    public TokenExpiredException() {
        super(ErrorCodeEnums.TOKEN_EXPIRED.name());
    }
}
