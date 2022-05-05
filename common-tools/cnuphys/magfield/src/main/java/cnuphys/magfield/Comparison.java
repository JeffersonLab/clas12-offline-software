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
	int sect = 1;
	
	public float[] oldResult = {Float.NaN, Float.NaN, Float.NaN};
	public float[] newResult = {Float.NaN, Float.NaN, Float.NaN};
	
	public float maxCompDiff() {
		float cmax = 0;
		for (int i = 0; i < 3; i++) {
			cmax = Math.max(cmax, Math.abs(oldResult[i] - newResult[i]));
		}
		return cmax;
	}
	
	public String toString() {
		return String.format("[%d] (%8.4e, %8.4e, %8.4e), old: (%8.4e, %8.4e, %8.4e), new: (%8.4e, %8.4e, %8.4e) [%10.6e]", sect, x, y, z, 
				oldResult[0], oldResult[1], oldResult[2], 
				newResult[0], newResult[1], newResult[2], maxCompDiff());
	}
}
