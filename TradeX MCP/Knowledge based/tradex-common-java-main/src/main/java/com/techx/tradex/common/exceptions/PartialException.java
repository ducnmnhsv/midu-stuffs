package com.techx.tradex.common.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartialException extends RuntimeException {
    private Object data;
    private Exception e;

    public String getMessage() {
        return e.getMessage();
    }
}
