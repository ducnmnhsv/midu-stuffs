package com.techx.tradex.common.exceptions;

import java.util.ArrayList;

public class NoQuerySessionException extends GeneralException {
    public NoQuerySessionException() {
        super("NO_QUERY_SESSION", new ArrayList<>());
    }
}
