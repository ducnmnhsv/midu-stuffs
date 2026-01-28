package com.techx.tradex.common.utils;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.subjects.Subject;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("squid:S1610")
public abstract class BaseObservableTxFactory {
    public final <T> Observable<T> create(Observable.OnSubscribe<T> f) {
        return new ObservableTx(this, f);
    }

    public final <T, F> Observable<T> create(CompletableFuture<F> completableFuture, BiConsumer<Observer<? super T>, F> f) {
        return new ObservableTx<>(this, subscriber ->
            completableFuture.handleAsync((data, err) -> {
                if (err != null) {
                    subscriber.onError(err);
                    subscriber.onCompleted();
                } else {
                    try {
                        call(f, subscriber, data);
                    } catch (Exception e) {
                        subscriber.onError(e);
                        subscriber.onCompleted();
                    }
                }
                return data;
            })
        );
    }

    public final <T> void transform(Subject<T, ?> subject, Consumer<Subject<T, ?>> f) {
        new ObservableTx(this, os -> f.accept(subject));
    }

    /**
     * please add transactional to this method on implementation
     *
     * @param onSubscribe
     * @param subscriber
     */
    public void call(Observable.OnSubscribe onSubscribe, Subscriber subscriber) {
        try {
            onSubscribe.call(subscriber);
        } catch (Exception e) {
            subscriber.onError(e);
            subscriber.onCompleted();
        }
    }

    /**
     * please mark @transactional to this method
     * @param st
     * @param data
     * @param <T>
     * @param <F>
     */
    public <T, F> void call(BiConsumer<Observer<? super T>, F> consumer, Subscriber<? super T> st, F data) {
        consumer.accept(st, data);
    }

    private static class ObservableTx<T> extends Observable<T> {
        public ObservableTx(BaseObservableTxFactory observableTxFactory, OnSubscribe<T> f) {
            super(new OnSubscribeDecorator<>(observableTxFactory, f));
        }
    }

    private static class OnSubscribeDecorator<T> implements Observable.OnSubscribe<T> {

        private final BaseObservableTxFactory observableTxFactory;
        private final Observable.OnSubscribe<T> onSubscribe;

        OnSubscribeDecorator(final BaseObservableTxFactory observableTxFactory, final Observable.OnSubscribe<T> s) {
            this.onSubscribe = s;
            this.observableTxFactory = observableTxFactory;
        }

        @Override
        public void call(Subscriber<? super T> subscriber) {
            observableTxFactory.call(onSubscribe, subscriber);
        }
    }
}
