package cnuphys.magfield;

/**
 * Length unit, either CM or M. These are for lengths in field map files.
 * 
 * @author David Heddle
 * @author Nicole Schumacher
 * @version 1.0
 * 
 */

public enum LengthUnit {

	CM, M;

	/**
	 * Get the length unit from an integer
	 * 
	 * @param val the integer value
	 * @return the length unit
	 */
	public static LengthUnit fromInt(int val) {
		if (val == 0) {
			return CM;
		} else if (val == 1) {
			return M;
		}
		return null;
	}

}
