package cnuphys.bayes;

import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("serial")
public class EventDatabase extends ArrayList<Event> {
	
	//the singleton
	private static EventDatabase instance;
	
	private EventDatabase() {
		super();
	}
	
	/**
	 * Accessor for the singleton database
	 * @return the singleton database
	 */
	public static EventDatabase getInstance() {
		if (instance == null) {
			instance = new EventDatabase();
		}
		return instance;
	}
	
	/**
	 * Adds and sorts.
	 * @param event the event to add
	 * @throws BayesException if event is null or if it is a duplicate
	 */
	public void addEvent(Event event) throws BayesException {
		if (event == null) {
			throw new BayesException("Tried to add null event");
		}
		
		int index = Collections.binarySearch(instance, event);
		if (index >= 0) {
			throw new BayesException("Tried to add non-unique event: [" + event.getAbbreviation() + "]");
		}
		index = -(index + 1); // now the insertion point.
		super.add(index, event);
	}
	
}
