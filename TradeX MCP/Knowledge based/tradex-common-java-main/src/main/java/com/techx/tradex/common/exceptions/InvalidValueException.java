package com.techx.tradex.common.exceptions;


import com.techx.tradex.common.constants.ErrorCodeEnums;

import java.util.Collections;

public class InvalidValueException extends InvalidParameterException {
    public InvalidValueException(String fieldName) {
        super();
        this.add(new FieldError(
                ErrorCodeEnums.INVALID_VALUE.name(),
                fieldName,
                Collections.singletonList(fieldName)
        ));
    }
}
