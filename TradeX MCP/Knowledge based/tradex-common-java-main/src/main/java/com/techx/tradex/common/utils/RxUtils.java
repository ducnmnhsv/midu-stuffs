package com.techx.tradex.common.utils;

import com.techx.tradex.common.exceptions.GeneralException;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class RxUtils {
    private RxUtils() {
    }

    public static <T> void onNext(Observer<T> subject, T data) {
        subject.onNext(data);
        subject.onCompleted();
    }

    public static <T> void onErrorTransform(Observer<T> subject, Throwable throwable) {
        if (throwable instanceof GeneralException) {
            onError(subject, throwable);
        } else {
            onError(subject, new GeneralException().source(throwable));
        }
    }

    public static <T> void onError(Observer<T> subject, Throwable throwable) {
        subject.onError(throwable);
        subject.onCompleted();
    }

    public static <T> void handleFuture(CompletableFuture<T> future, Observer<T> subject) {
        future.handleAsync((data, err) -> {
            if (err != null) {
                onError(subject, err);
                return null;
            }
            onNext(subject, data);
            return data;
        });
    }

    public static <F> void handleFuture(
            CompletableFuture<F> future
            , Consumer<F> func
            , Consumer<Throwable> errorHandler
    ) {
        future.handleAsync((data, err) -> {
            if (err != null) {
                errorHandler.accept(err);
                return null;
            }
            func.accept(data);
            return data;
        });
    }

    /**
     * handle multiple futures
     *
     * @param func:    when you want to end operation then just call operator.operate()
     * @param finish:  this method will be called when all futures has been consumed and end is ot yet reached.
     * @param futures: array of futures
     */
    public static <T> void handleFutures(
            Observer<T> observer
            , BiConsumer<Object, Operator> func
            , Consumer<List<Object>> finish
            , CompletableFuture<? extends Object>... futures
    ) {
        List<Object> results = new ArrayList<>();
        AtomicBoolean end = new AtomicBoolean(false);
        Operator operator = () -> end.set(true);
        for (CompletableFuture<? extends Object> future : futures) {
            future.handleAsync((data, err) -> {
                results.add(data);
                if (!end.get()) {
                    if (err != null) {
                        onError(observer, err);
                        operator.operate();
                        return null;
                    }
                    func.accept(data, operator);
                }
                if (!end.get() && results.size() == futures.length) {
                    finish.accept(results);
                }
                return data;
            });
        }

    }

    public static <T, F> void handleFutureError(
            CompletableFuture<F> future
            , Observer<T> observer
            , Consumer<F> func
    ) {
        future.handleAsync((data, err) -> {
            if (err != null) {
                if (err instanceof CompletionException && err.getCause() != null) {
                    onError(observer, err.getCause());
                } else {
                    onError(observer, err);
                }
                return null;
            }
            func.accept(data);
            return data;
        });
    }

    public static <T> void transform(Observable<T> from, Observer<T> to) {
        from.subscribe(
                to::onNext,
                to::onError,
                to::onCompleted
        );
    }

    public static <T, V> void transform(Observable<T> from, Observer<V> to, Function<T, V> transform) {
        from.subscribe(
                t -> to.onNext(transform.apply(t)),
                to::onError,
                to::onCompleted
        );
    }

    public static <T> void safeSubscribe(Observable<T> observable, Action1<T> onNext, Action1<Throwable> onError) {
        observable.subscribe(
                t -> {
                    try {
                        onNext.call(t);
                    } catch (Exception e) {
                        onError.call(e);
                    }
                },
                onError
        );
    }

    public static <T> void safeSubscribeAll(Action1<T> onNext, Action1<Throwable> onError, Action0 onComplete, Observable<T> ...observables) {
        final AtomicInteger totalResult = new AtomicInteger(0);
        final int total = observables.length;
        Action0 complete = () -> {
            if (totalResult.get() >= total) {
                onComplete.call();
            }
        };
        for (Observable<T> observable : observables) {
            observable.subscribe(
                    t -> {
                        try {
                            onNext.call(t);
                            totalResult.incrementAndGet();
                        } catch (Exception e) {
                            onError.call(e);
                        }
                    },
                    err -> {
                        try {
                            onError.call(err);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        totalResult.incrementAndGet();
                        complete.call();
                    },
                    complete
            );
        }
    }

    public static <T> void safeSubscribe(Observable<T> observable, Action1<T> onNext, Action1<Throwable> onError, Action0 onComplete) {
        observable.subscribe(
                t -> {
                    try {
                        onNext.call(t);
                    } catch (Exception e) {
                        onError.call(e);
                    }
                },
                err -> {
                    try {
                        onError.call(err);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    if (onComplete != null) {
                        onComplete.call();
                    }
                },
                onComplete
        );
    }

    public static <T> CompletableFuture<T> parse(
            Observable<T> observable
    ) {
        CompletableFuture<T> future = new CompletableFuture<>();
        observable.subscribe(
                future::complete,
                future::completeExceptionally
        );
        return future;
    }
}
