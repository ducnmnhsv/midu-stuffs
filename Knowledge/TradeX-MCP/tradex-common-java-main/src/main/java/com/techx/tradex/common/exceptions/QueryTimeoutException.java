package com.techx.tradex.common.exceptions;

public class QueryTimeoutException extends GeneralException {
    public QueryTimeoutException() {
        super("QUERY_TIMEOUT");
    }
}
