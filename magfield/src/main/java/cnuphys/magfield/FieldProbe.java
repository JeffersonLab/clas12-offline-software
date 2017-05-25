package cnuphys.magfield;

public class FieldProbe {

	protected static boolean CACHE = true;
	
	protected static final double TINY = 1.0e-8;

	/**
	 * Turn the caching on or off globally
	 * 
	 * @param cacheOn
	 *            the value of the flag
	 */
	public static void cache(boolean cacheOn) {
		CACHE = cacheOn;
	}

}
