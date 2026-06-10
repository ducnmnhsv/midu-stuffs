package com.techx.tradex.realtime.model;


import org.slf4j.Logger;

import java.util.function.Consumer;

public class MonitorStatistic<T> {
    private final String name;
    private final Consumer<T> consumer;
    private final Consumer<Throwable> errConsumer;
    private final Statistic current = new Statistic();
    private Statistic lastLogged;

    public MonitorStatistic(String name, Consumer<T> consumer, Consumer<Throwable> errConsumer) {
        this.name = name;
        this.consumer = consumer;
        this.errConsumer = errConsumer;
    }

    public void process(T item) {
        long startTime = System.currentTimeMillis();
        try {
            consumer.accept(item);
        } catch (Throwable e) {
            this.errConsumer.accept(e);
        }
        long time = System.currentTimeMillis() - startTime;
        this.current.totalTimeInMs += time;
        this.current.totalProcessed++;
    }

    public void log(Logger logger) {
        Statistic compare = this.current.compare(this.lastLogged);
        if (this.lastLogged == null) {
            this.lastLogged = new Statistic(this.current);
        } else {
            this.lastLogged.copy(this.current);
        }
        logger.info("statistic {} total processed {} in {} ms. processed {} in {} ms", this.name,
                this.current.totalProcessed, this.current.totalTimeInMs, compare.totalProcessed, compare.totalTimeInMs);
    }

    public static class Statistic {
        private long totalProcessed;
        private long totalTimeInMs;

        public Statistic() {
        }

        public Statistic(long totalProcessed, long totalTimeInMs) {
            this.totalProcessed = totalProcessed;
            this.totalTimeInMs = totalTimeInMs;
        }

        public Statistic(Statistic copy) {
            this.totalProcessed = copy.totalProcessed;
            this.totalTimeInMs = copy.totalTimeInMs;
        }

        public void copy(Statistic statistic) {
            this.totalProcessed = statistic.totalProcessed;
            this.totalTimeInMs = statistic.totalTimeInMs;
        }

        private Statistic compare(Statistic last) {
            if (last == null) {
                return this;
            }
            return new Statistic(this.totalProcessed - last.totalProcessed, this.totalTimeInMs - last.totalTimeInMs);
        }
    }
}
