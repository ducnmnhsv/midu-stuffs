package com.techx.tradex.common.exceptions;

import java.util.ArrayList;

public class NoMoreRecordException extends GeneralException {
    public NoMoreRecordException() {
        super("NO_MORE_RECORDS", new ArrayList<>());
    }
}
