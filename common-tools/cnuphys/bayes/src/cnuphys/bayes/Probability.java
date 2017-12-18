package cnuphys.bayes;

public class Probability {

	private double _value = Double.NaN;
	
	/**
	 * Create a probability witha default NaN value
	 */
	public Probability() {
	}
	
	/**
	 * Create a probability with a valid value [0, 1]
	 * @param value the value of the probability
	 * @throws BayesException if the value is out of range
	 */
	public Probability(double value) throws BayesException {
		setValue(value);
	}
	
	/**
	 * Get the probability value
	 * @return the probability value either [0, 1] or NaN.
	 */
	public double value() {
		return _value;
	}
	
	/**
	 * Set the probability a valid value [0, 1]
	 * @param value the value of the probability
	 * @throws BayesException if the value is out of range
	 */
	public void setValue(double value) throws BayesException {
		if ((value < 0) || (value > 1.0)) {
			throw new BayesException("Bad value for probability: " + value);
		}
		_value = value;
	}
	
	/**
	 * Check whether we have a valid value [0, 1]
	 * @return <code>true</code> if we have a valid value
	 */
	public boolean valid() {
		return !((_value < 0) || (_value > 1.0));
	}
}
