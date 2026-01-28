package com.techx.tradex.common.exceptions;

import com.techx.tradex.common.constants.ErrorCodeEnums;
import lombok.Data;

@Data
public class InvalidParameterException extends SubErrorsException {
    public InvalidParameterException() {
        super(ErrorCodeEnums.INVALID_PARAMETER.name());
    }
}
