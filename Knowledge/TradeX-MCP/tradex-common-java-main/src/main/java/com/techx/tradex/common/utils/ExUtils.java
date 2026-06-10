package com.techx.tradex.common.utils;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import rx.functions.Action1;

public class ExUtils {
    public static void operate(Operator operator, Action1<Exception> handler) {
        try {
            operator.operate();
        } catch (Exception e) {
            handler.call(e);
        }
    }

    public static <T> void foreach(Iterable<T> it, Consumer<T> consumer, Action1<Exception> handler) {
        foreach(it, consumer, null, handler);
    }

    public static <T> void foreach(Iterable<T> it, Consumer<T> consumer, BiFunction<T, Integer, Boolean> stopCondition, Action1<Exception> handler) {
        Iterator<T> i = it.iterator();
        int index = 0;
        while(i.hasNext()) {
            try {
                T t = i.next();
                if (stopCondition != null && stopCondition.apply(t, index)) {
                    break;
                }
                consumer.accept(t);
                index++;
            } catch (Exception e) {
                handler.call(e);
            }
        }
    }
}