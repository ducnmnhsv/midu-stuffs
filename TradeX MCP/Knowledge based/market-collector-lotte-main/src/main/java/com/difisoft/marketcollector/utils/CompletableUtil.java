package com.difisoft.marketcollector.utils;

import java.util.concurrent.CompletableFuture;

public class CompletableUtil {
    public static <T> CompletableFuture<T> exception(Throwable e) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(e);
        return result;
    }
}
