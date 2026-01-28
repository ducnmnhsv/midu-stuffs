package com.techx.tradex.common.exceptions;


import com.techx.tradex.common.constants.ErrorCodeEnums;

import java.util.Arrays;
import java.util.Collections;

public class MustGreaterException extends InvalidParameterException {
    public MustGreaterException(String fieldName, String minimum) {
        super();
        this.add(new FieldError(
                ErrorCodeEnums.VALUE_MUST_GREATER.name(),
                fieldName,
                Arrays.asList(fieldName, minimum)
        ));
    }
}
