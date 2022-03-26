package cnuphys.magfield;

/**
 * Used for testing only
 * @author heddle
 *
 */
public class Comparison {
	public float x;
	public float y;
	public float z;
	
	public float[] tradResult = {Float.NaN, Float.NaN, Float.NaN};
	public float[] newResult = {Float.NaN, Float.NaN, Float.NaN};
	
	
	public String toString() {
		return String.format("(%9.5e, %9.5e, %9.5e), old: (%9.5e, %9.5e, %9.5e), new: (%9.5e, %9.5e, %9.5e)", x, y, z, 
				tradResult[0], tradResult[1], tradResult[2], 
				newResult[0], newResult[1], newResult[2]);
	}
}
