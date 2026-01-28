package com.techx.tradex.common.utils;

import lombok.Data;

@Data
public class Wrapper<T> {
    public static <T> Wrapper<T> create(T value) {
        return new Wrapper<>(value);
    }

    private T v;

    private Wrapper(T v) {
        this.v = v;
    }
}
