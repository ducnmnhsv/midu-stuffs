package com.difisoft.nhsv.admin.utils;

import com.difisoft.model.utils.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompletableUtil {

    private static final Logger log = LoggerFactory.getLogger(CompletableUtil.class);

    public static <T> CompletableFuture<T> exception(Throwable e) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(e);
        return result;
    }

    public static <T> CompletableFuture<List<Pair<T, Throwable>>> waitForAllFinish(Collection<CompletableFuture<T>> cfs) {
        CompletableFuture<List<Pair<T, Throwable>>> future = new CompletableFuture<>();
        AtomicInteger completedSize = new AtomicInteger(0);
        List<Pair<T, Throwable>> results = new ArrayList<>();
        int index = 0;
        for (CompletableFuture<T> cf : cfs) {
            index++;
            int finalIndex = index;
            cf.handle((k, e) -> {
                log.info("future {} completed", finalIndex);
                results.add(new Pair<>(k, e));
                if (completedSize.incrementAndGet() == cfs.size()) {
                    future.complete(results);
                }
                return null;
            });
        }
        return future;
    }
    //    public static <T> CompletableFuture<T> await(CompletableFuture<T> future) {
    //        try {
    //            T result = Async.await(future);
    //            return CompletableFuture.completedFuture(result);
    //        } catch (Exception e) {
    //            if (e instanceof CompletionException) {
    //                return exception(e.getCause());
    //            }
    //            return exception(e);
    //        }
    //    }
    //
    //    public static <T> T await(CompletableFuture<T> future, CompletableFuture<T> rethrowFuture) {
    //        try {
    //            T result = Async.await(future);
    //            return result;
    //        } catch (Exception e) {
    //            if (e instanceof CompletionException) {
    //                return exception(e.getCause());
    //            }
    //            return exception(e);
    //        }
    //    }
}
