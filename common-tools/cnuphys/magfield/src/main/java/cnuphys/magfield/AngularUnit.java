/*
 * 
 */
package cnuphys.magfield;

/**
 * Angular units either DEGREES or RADIANS.
 * 
 * @author David Heddle
 * @author Nicole Schumacher
 * @version 1.0
 */

public enum AngularUnit {

	DEGREES, RADIANS;

	/**
	 * Get the angular unit from an integer
	 * 
	 * @param val the integer value
	 * @return the angular unit
	 */
	public static AngularUnit fromInt(int val) {
		if (val == 0) {
			return DEGREES;
		} else if (val == 1) {
			return RADIANS;
		}
		return null;
	}

}
