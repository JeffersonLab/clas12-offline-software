package cnuphys.eventStation;

import java.util.Stack;

import org.jlab.io.base.DataEvent;

/**
 * A LIFO queue (stack) for "next" events
 * @author heddle
 *
 */
public class EventLIFO extends Stack<DataEvent> {
	
	//the queue capacity
	private int _capacity;

	public EventLIFO(int capacity) {
		_capacity = capacity;
	}
	
	/**
	 * Is the LIFO at the desired capacity
	 * @return
	 */
	public boolean isFull() {
		return size() > _capacity;
	}
}
