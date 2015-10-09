package cnuphys.bCNU.component.rangeslider;

public interface IValueLabeler {

	/**
	 * Take a value and convert it to a string
	 * 
	 * @param value
	 *            the value
	 * @return the string for a label.
	 */
	public String valueString(long value);
}
