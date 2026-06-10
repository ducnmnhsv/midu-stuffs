package com.techx.tradex.common.utils;


public interface DoubleFunction<R, P1, P2> {
    R call(P1 p1, P2 p2);
}
