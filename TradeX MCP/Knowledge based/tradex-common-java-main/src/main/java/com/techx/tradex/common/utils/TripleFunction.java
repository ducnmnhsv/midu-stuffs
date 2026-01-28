package com.techx.tradex.common.utils;


public interface TripleFunction<R, P1, P2, P3> {
    R call(P1 p1, P2 p2, P3 p3);
}
