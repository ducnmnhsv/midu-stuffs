package com.techx.tradex.common.exceptions;

import com.techx.tradex.common.constants.ErrorCodeEnums;
import lombok.Data;

import java.util.Collections;

@Data
public class NotFoundException extends GeneralException {
    public NotFoundException(String objectName) {
        super(ErrorCodeEnums.TARGET_NOT_FOUND.name(), Collections.singletonList(objectName));
    }
}
