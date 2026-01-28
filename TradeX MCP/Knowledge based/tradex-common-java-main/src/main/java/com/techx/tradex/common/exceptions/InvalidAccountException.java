package com.techx.tradex.common.exceptions;

import com.techx.tradex.common.constants.ErrorCodeEnums;

public class InvalidAccountException extends GeneralException {
    public InvalidAccountException() {
        super(ErrorCodeEnums.INVALID_ACCOUNT.name());
    }
}
