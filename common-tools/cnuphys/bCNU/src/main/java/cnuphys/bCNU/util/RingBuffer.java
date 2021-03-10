package cnuphys.bCNU.util;

import java.util.LinkedList;

/**
 * A standard FIFO ring buffer
 * @author davidheddle
 *
 * @param <T>
 */
public class RingBuffer<T> extends LinkedList<T> {
	
	//capacity of the buffer
	private int _capacity;
	
	/**
	 * Create a RingBuffer
	 * @param capacity the capacity of the buffer
	 */
	public RingBuffer(int capacity) {
		super();
		_capacity = capacity;
	}
	
	/**
	 * Clear the data and reset.
	 */
	@Override
	public void clear() {
		super.clear();
	}
	
	@Override
	public boolean add(T elem) {
		addFirst(elem);
		return true;
	}
	
	@Override
	public void addFirst(T elem) {	
		
		//don't allow nulls
		if (elem == null) {
			return;
		}
		
		//full? remove last
		if (isFull()) {
			removeLast();
		}
		
		super.addFirst(elem);
	}
	
	/**
	 * Is the buffer full?
	 * @return <code>true</code> if the buffer is full.
	 */
	public boolean isFull() {
		return size() == _capacity;
	}
	
	/**
	 * get the previous element. This would be the next older element,
	 * modulo the oldest, at which point you'd get the newest (which may not be the current) 
	 * until you get back to yourself.
	 * @return
	 */
	public T previous() {
		if (size() == 0) {
			return null;
		}
		
		T elem = this.removeFirst();
		
		addLast(elem);
		

		return elem;
		
	}
}
