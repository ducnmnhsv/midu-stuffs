package com.techx.tradex.common.utils;

import java.util.concurrent.CompletableFuture;

public class CompletionUtils {
    public static <T> CompletableFuture<T> exception(Throwable e) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(e);
        return result;
    }
}
