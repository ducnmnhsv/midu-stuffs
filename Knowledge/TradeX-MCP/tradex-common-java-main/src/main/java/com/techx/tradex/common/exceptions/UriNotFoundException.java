package com.techx.tradex.common.exceptions;


import com.techx.tradex.common.constants.ErrorCodeEnums;

public class UriNotFoundException extends GeneralException {
    public UriNotFoundException() {
        super(ErrorCodeEnums.URI_NOT_FOUND.name());
    }
}
