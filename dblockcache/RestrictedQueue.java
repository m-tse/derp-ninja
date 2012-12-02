package dblockcache;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/*
 * Vamsi - Needed?
 */


public class RestrictedQueue<T> implements Queue<T> {

	private int maxSize;
	private LinkedList<T> queue;
	
	
	public RestrictedQueue(int maxSize) {
		this.maxSize = maxSize;
		this.queue = new LinkedList<T>();
	}
	
	
	@Override 
	public boolean addAll(Collection<? extends T> c) { 
		Iterator<? extends T> iterator = c.iterator();
		int elementsAdded = 0;
		while (iterator.hasNext()) {
			if (elementsAdded == this.maxSize) {
				return false;
			} else if (elementsAdded > this.maxSize) {
				System.err.println("RestrictedQueue.addAll over max size");
				return false;
			}
			this.queue.add(iterator.next());
			++elementsAdded;
		}
		return true;
	}


	public boolean containsAll(Collection<?> c) {
		if (c.size() >= this.maxSize) {
			return false;
		}
		return this.queue.containsAll(c);
	}


	@Override
	public boolean removeAll(Collection<?> c) {
		return this.queue.removeAll(c);
	}


	@Override
	public boolean retainAll(Collection<?> c) {
		return this.queue.retainAll(c);
	}


	@Override
	public <T> T[] toArray(T[] a) {
		return this.queue.toArray(a);
	}


	@Override
	public boolean add(T arg0) {
		if (this.size() == this.maxSize) {
			return false;
		} else if (this.size() > this.maxSize) {
			System.err.println("RestrictedQueue.add() exceeded max size");
			return false;
		}
		return this.queue.add(arg0);
	}


	@Override
	public boolean offer(T arg0) {
		if (this.size() == this.maxSize) {
			return false;
		} else if (this.size() > this.maxSize) {
			System.err.println("RestrictedQueue.add() exceeded max size");
			return false;
		}
		return this.queue.offer(arg0);
	}


	@Override
	public void clear() {
		this.queue.clear();
	}


	@Override
	public boolean contains(Object o) {
		return this.queue.contains(o);
	}


	@Override
	public boolean isEmpty() {
		return this.queue.isEmpty();
	}


	@Override
	public Iterator<T> iterator() {
		return this.queue.iterator();
	}


	@Override
	public boolean remove(Object o) {
		return this.queue.remove(o);
	}


	@Override
	public int size() {
		return this.queue.size();
	}


	@Override
	public Object[] toArray() {
		return this.queue.toArray();
	}


	@Override
	public T element() {
		return this.queue.element();
	}


	@Override
	public T peek() {
		return this.queue.peek();
	}


	@Override
	public T poll() {
		return this.queue.poll();
	}


	@Override
	public T remove() {
		return this.queue.remove();
	}
	
}
