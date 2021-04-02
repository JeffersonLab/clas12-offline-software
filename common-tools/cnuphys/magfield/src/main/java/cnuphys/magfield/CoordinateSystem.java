/*
 * 
 */
package cnuphys.magfield;

/**
 * Different coordinate systems for either field or grid, for data in field map
 * files.
 * 
 * @author David Heddle
 * @author Nicole Schumacher
 * @version 1.0
 */

public enum CoordinateSystem {

	CYLINDRICAL, CARTESIAN;

	/**
	 * Get the coordinate system from an integer
	 * 
	 * @param val the integer value
	 * @return the coordinate system
	 */
	public static CoordinateSystem fromInt(int val) {
		if (val == 0) {
			return CYLINDRICAL;
		} else if (val == 1) {
			return CARTESIAN;
		}
		return null;
	}

}
