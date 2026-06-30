package com.difisoft.nhsv.admin.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class LimitedSizeQueue<T> {

    private final int maxSize;
    private AtomicInteger size = new AtomicInteger(0);
    private Node<T> first;
    private Node<T> last;

    public LimitedSizeQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    public synchronized void add(T body) {
        Node<T> node = new Node<>();
        node.body = body;
        if (last == null) {
            this.last = node;
            this.first = node;
        } else {
            this.last.next = node;
            this.last = node;
        }
        if (size.incrementAndGet() > this.maxSize) {
            this.first = this.first.next;
            size.set(this.maxSize);
        }
    }

    public void forEach(Consumer<T> consumer) {
        if (this.first == null) {
            return;
        }
        Node<T> node = this.first;
        while (node != null) {
            consumer.accept(node.body);
            node = node.next;
        }
    }

    private static class Node<N> {

        N body;
        Node<N> next;
    }
}
