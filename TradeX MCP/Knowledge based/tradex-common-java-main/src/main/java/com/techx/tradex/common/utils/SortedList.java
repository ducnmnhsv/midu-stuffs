package com.techx.tradex.common.utils;

import java.util.*;

public class SortedList<T extends Comparable<T>> implements List<T> {
    protected LinkedList<T> realList;
    protected boolean nullFirst = true;

    public SortedList() {
        this.realList = new LinkedList<>();
    }

    public SortedList<T> nullLast() {
        this.nullFirst = false;
        return this;
    }

    @Override
    public int size() {
        return this.realList.size();
    }

    @Override
    public boolean isEmpty() {
        return this.realList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.realList.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return this.realList.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.realList.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return this.realList.toArray(a);
    }

    @Override
    public boolean add(T t) {
        if (t == null) {
            if (this.nullFirst) {
                this.realList.addFirst(t);
            } else {
                this.realList.addLast(t);
            }
        } else if (this.isEmpty()) {
            return this.realList.add(t);
        } else {
            Iterator<T> it = this.iterator();
            int index = 0;
            while (it.hasNext()) {
                if (t.compareTo(it.next()) < 0) {
                    this.realList.add(index, t);
                    return true;
                }
                index++;
            }
            this.realList.addLast(t);
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return this.realList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.realList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) {
            boolean res = this.add(t);
            if (!res) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.realList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.realList.retainAll(c);
    }

    @Override
    public void clear() {
        this.realList.clear();
    }

    @Override
    public T get(int index) {
        return this.realList.get(index);
    }

    @Override
    public T set(int index, T element) {
        return this.realList.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return this.realList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.realList.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return this.realList.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return this.realList.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return this.realList.subList(fromIndex, toIndex);
    }
}
