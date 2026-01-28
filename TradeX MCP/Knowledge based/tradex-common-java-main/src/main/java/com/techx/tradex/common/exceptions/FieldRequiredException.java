package com.techx.tradex.common.exceptions;


import com.techx.tradex.common.constants.ErrorCodeEnums;

import java.util.Collections;

public class FieldRequiredException extends InvalidParameterException {
    public FieldRequiredException(String fieldName) {
        super();
        this.add(new FieldError(
                ErrorCodeEnums.EMPTY_VALUE.name(),
                fieldName,
                Collections.singletonList(fieldName)
        ));
    }
}
