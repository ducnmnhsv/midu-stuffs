package com.techx.tradex.common.utils.validator;

public interface IValidator<T> {
    Object valid(T t);
}
