/*
 * 
 */
package cnuphys.magfield;

/**
 * Units for the magnetic field in a field map file.
 * 
 * @author Dr. David Heddle
 * @author Nicole Schumacher
 * @version 1.0
 */
public enum FieldUnit {

	kG, G, T;

	/**
	 * Get the field unit from an integer
	 * 
	 * @param val the integer value
	 * @return the field unit
	 */
	public static FieldUnit fromInt(int val) {
		if (val == 0) {
			return kG;
		} else if (val == 1) {
			return G;
		} else if (val == 2) {
			return T;
		}
		return null;
	}

}
