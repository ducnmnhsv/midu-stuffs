package com.techx.tradex.common.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

@Getter
public class UnSubscribeAble<T> {
    private Subscription subscription;
    private Observable<T> observable;

    public UnSubscribeAble(Observable<T> observable) {
        this.observable = observable;
    }

    public void subscribe(Action1<Data<T>> onNext) {
        this.subscription = observable.subscribe(t -> this.handleOnNext(t, onNext));
    }

    public void subscribe(Action1<Data<T>> onNext, Action1<Error> onError) {
        this.subscription = observable.subscribe(t -> this.handleOnNext(t, onNext), err -> this.handleOnError(err, onError));
    }

    public void subscribe(
            Action1<Data<T>> onNext,
            Action1<Error> onError,
            Action0 onCompleted
    ) {
        if (onError == null) {
            this.subscription = observable.subscribe(t -> this.handleOnNext(t, onNext));
        } else if (onCompleted == null) {
            this.subscription = observable.subscribe(
                    t -> this.handleOnNext(t, onNext),
                    err -> this.handleOnError(err, onError)
            );
        } else {
            this.subscription = observable.subscribe(
                    t -> this.handleOnNext(t, onNext),
                    err -> this.handleOnError(err, onError),
                    onCompleted
            );
        }
    }

    private void handleOnNext(T t, Action1<Data<T>> onNext) {
        Data<T> data = new Data<>(this.subscription, t);
        onNext.call(data);
    }

    private void handleOnError(Throwable throwable, Action1<Error> onError) {
        Error error = new Error(this.subscription, throwable);
        onError.call(error);
    }

    @lombok.Data
    @AllArgsConstructor
    public static class Data<T> {
        private Subscription subscription;
        private T data;
    }

    @lombok.Data
    @AllArgsConstructor
    public static class Error extends Throwable {
        private Subscription subscription;
        private Throwable source;
    }
}
