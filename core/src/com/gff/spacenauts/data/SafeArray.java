package com.gff.spacenauts.data;

import com.badlogic.gdx.utils.Array;

public class SafeArray<T> {

	private Array<T> internalArray;
	private final int MAX_SIZE;
	
	public SafeArray () { 
		internalArray = new Array<T>();
		MAX_SIZE = 0;
	}
	
	public SafeArray (int initialCapacity) {
		internalArray = new Array<T>(initialCapacity);
		MAX_SIZE = 0;
	}
	
	public SafeArray (int initialCapacity, boolean sorted) {
		internalArray = new Array<T>(sorted, initialCapacity);
		MAX_SIZE = 0;
	}
	
	public SafeArray (int initialCapacity, boolean sorted, int maxSize) {
		internalArray = new Array<T>(sorted, initialCapacity);
		MAX_SIZE = maxSize;
	}
	
	synchronized public T get (int i) {
		while(isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return internalArray.get(i);
	}
	
	synchronized public T pop () {
		while (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		T ret = internalArray.pop();
		notifyAll();
		return ret;
	}
	
	synchronized public T first () {
		while (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return internalArray.first();
	}
	
	synchronized public T popFirst () {
		while (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		T ret = internalArray.first();
		internalArray.removeIndex(0);
		notifyAll();
		return ret;
	}
	
	synchronized public void add (T item) {
		while (isFull()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		internalArray.add(item);
		notifyAll();
	}
	
	synchronized public void insert (T item, int index) {
		while (isFull()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		internalArray.insert(index, item);
		notifyAll();
	}
	
	synchronized public boolean isEmpty() {
		return internalArray.size == 0;
	}
	
	synchronized public boolean isFull() {
		if (MAX_SIZE != 0 && internalArray.size == MAX_SIZE) {
			return true;
		} else {
			return false;
		}
	}
}
