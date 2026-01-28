package com.techx.tradex.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Union<T, R> {
    public static <T, R> Union<T, R> create1(T t) {
        return new Union(t, null);
    }

    public static <T, R> Union<T, R> create2(R r) {
        return new Union(null, r);
    }

    protected T type1;
    protected R type2;

    public boolean isNoValue() {
        return this.type1 == null && this.type2 == null;
    }
}
