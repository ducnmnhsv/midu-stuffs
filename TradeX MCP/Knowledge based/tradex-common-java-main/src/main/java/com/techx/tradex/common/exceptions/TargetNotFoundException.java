package com.techx.tradex.common.exceptions;


import com.techx.tradex.common.constants.ErrorCodeEnums;

public class TargetNotFoundException extends GeneralException {
    public TargetNotFoundException() {
        super(ErrorCodeEnums.TARGET_NOT_FOUND.name());
    }
}
