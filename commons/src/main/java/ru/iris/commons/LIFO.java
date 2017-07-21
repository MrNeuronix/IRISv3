package ru.iris.commons;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by nix on 15.11.2016.
 */
public class LIFO<T> extends LinkedBlockingDeque<T> {

    private final ReentrantLock lock = new ReentrantLock();

    public LIFO() {
        super(5);
    }

    public LIFO(int capacity) {
        super(capacity);
    }

    @Override
    public synchronized boolean add(T e) {

        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (super.remainingCapacity() == 0)
                super.removeLast();
            return super.add(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public synchronized void addFirst(T e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (super.remainingCapacity() == 0)
                super.removeLast();
            super.addFirst(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public synchronized void addLast(T e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (super.remainingCapacity() == 0)
                super.removeLast();
            super.addLast(e);
        } finally {
            lock.unlock();
        }
    }
}
