package com.techx.tradex.common.utils;

public interface Transform<S, D> {
    void parse(S s);

    default D from(S s) {
        this.parse(s);
        return (D) this;
    }
}
