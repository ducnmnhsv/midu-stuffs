package com.techx.tradex.common.exceptions;


import com.techx.tradex.common.constants.ErrorCodeEnums;

public class StockNotFoundException extends GeneralException {
    public StockNotFoundException() {
        super(ErrorCodeEnums.INDEX_NOT_FOUND.name());
    }
}
