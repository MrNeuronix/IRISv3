package ru.iris.commons;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by nix on 15.11.2016.
 */
public class LIFO<T> extends LinkedBlockingDeque<T> {

	public LIFO() {
		super(10);
	}

	public LIFO(int capacity) {
		super(capacity);
	}

	@Override
	public synchronized boolean add(T e) {

		if(super.remainingCapacity() == 0)
			super.removeLast();

		return super.add(e);
	}

	@Override
	public synchronized void addFirst(T e) {

		if(super.remainingCapacity() == 0)
			super.removeLast();

		super.addFirst(e);
	}

	@Override
	public synchronized void addLast(T e) {

		if(super.remainingCapacity() == 0)
			super.removeLast();

		super.addLast(e);
	}
}
