package com.techx.tradex.common.utils.validator;

import com.techx.tradex.common.constants.ErrorCodeEnums;

import java.util.List;

public class ListValidator extends Validator<List> {
    public ListValidator(String fieldName, List fieldValue) {
        super(fieldName, fieldValue);
    }

    private boolean empty = false;

    public ListValidator empty() {
        this.empty = true;
        return this;
    }

    @Override
    protected Object doCheck() {
        Object result = null;
        if (this.empty) {
            if (this.fieldValue == null || this.fieldValue.isEmpty()) {
                return this.addError(ErrorCodeEnums.EMPTY_VALUE.name(), this.fieldName);
            }
        }
        return result;
    }
}
