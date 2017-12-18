package cnuphys.bayes;
/**
 * A probabilistic event. 
 * @author heddle
 *
 */

public class Event implements Comparable<Event> {
	
	//the longer description
	private String _description;
	
	//a unique abbreviation
	private String _abbreviation;
	
	//the probability of the event
	private Probability _probability;
	
	/**
	 * A probabilistic event
	 * @param description a description. for example "a royal flush of clubs"
	 * @param abbreviation and abbreviation used in equations, e.g. "RFS". Keep it short. It will be converted to all caps.
	 * It has to be unique for a given analysis
	 * @throws BayesException if the abbreviation is not unique.
	 */
	public Event(String description, String abbreviation) throws BayesException {
		_abbreviation = abbreviation.toUpperCase();
		_description = description;
		_probability = new Probability();
		EventDatabase.getInstance().addEvent(this);
	}

	//sort based on abbreviation
	@Override
	public int compareTo(Event other) {
		return _abbreviation.compareTo(other.getAbbreviation());
	}

	/**
	 * Get the longer description
	 * @return the description
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * Get the abbreviation
	 * @return the unique abbreviation
	 */
	public String getAbbreviation() {
		return _abbreviation;
	}
	
	/**
	 * Get the P form of the event, e.g., P(A)
	 * @return the P form of the event
	 */
	public String pForm() {
		return "P(" + _abbreviation + ")";
	}
	
	/**
	 * Get the P not form of the event, e.g., P(~A)
	 * @return the p form of the event
	 */
	public String pNotForm() {
		return "P(~" + _abbreviation + ")";
	}
	
	/**
	 * Get the form for the probability of this and another event.
	 * @param event the other event
	 * @return the "and" form, e.g. P(A,B)
	 */
	public String andForm(Event event) {
		return "P(" + _abbreviation + "," + event.getAbbreviation() + ")";
	}
	
	/**
	 * Get the form for the probability of this and multiple other
	 * @param event the other event
	 * @return the "and" form, e.g. P(A,B)
	 */
	public String andForm(Event... events) {
		int len = events.length;
		
		String s = "P(" + _abbreviation;
		
		if (len > 0) {
			for (Event event : events) {
				s += "," + event.getAbbreviation();
			}
		}
		
		s += ")";
		
		
		return s;
	}

	
	/**
	 * Get the form for the conditional probability of this and another event.
	 * @param event the other event
	 * @return the conditional form, e.g. P(A|B)
	 */
	public String conditionalForm(Event event) {
		return "P(" + _abbreviation + "|" + event.getAbbreviation() + ")";
	}
	
	/**
	 * Get the form for the conditional probability of this and another event.
	 * @param event the other event
	 * @return the conditional form, e.g. P(A|B)
	 */
	public String conditionalForm(Event... events) {
		int len = events.length;
		
		String s = "P(" + _abbreviation + "|";
		
		for (int i = 0; i < len; i++) {
			Event event = events[i];
			if (i == 0) {
				s += event.getAbbreviation();
			}
			else {
				s += "," + event.getAbbreviation();
			}
			
		}
		
		s += ")";
		
		return s;
	}



	/**
	 * Get the probability for this event
	 * @return the probability for this event, either [0, 1] or NaN
	 */
	public double probability() {
		return _probability.value();
	}
	
	/**
	 * Get the complement (1-probability) for this event
	 * @return the complement for this event, either [0, 1] or NaN
	 */
	public double complement() {
		return _probability.valid() ? (1.0 - probability()) : Double.NaN;
	}

	

}
