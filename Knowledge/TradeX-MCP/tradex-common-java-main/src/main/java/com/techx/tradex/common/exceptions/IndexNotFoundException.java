package com.techx.tradex.common.exceptions;


import com.techx.tradex.common.constants.ErrorCodeEnums;

public class IndexNotFoundException extends GeneralException {
    public IndexNotFoundException() {
        super(ErrorCodeEnums.INDEX_NOT_FOUND.name());
    }
}
