package com.techx.tradex.common.exceptions;


import com.techx.tradex.common.constants.ErrorCodeEnums;

import java.util.Collections;

public class InvalidFormatException extends InvalidParameterException {
    public InvalidFormatException(String fieldName) {
        super();
        this.add(new FieldError(
                ErrorCodeEnums.INVALID_FORMAT.name(),
                fieldName,
                Collections.singletonList(fieldName)
        ));
    }
}
