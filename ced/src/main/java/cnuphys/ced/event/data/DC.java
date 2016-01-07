package cnuphys.ced.event.data;

/**
 * static methods to centralize getting data arrays
 * @author heddle
 *
 */
public class DC {

	/**
	 * Get the pid array from the true data
	 * @return the pid array
	 */
	public static int[] pid() {
		return ColumnData.getIntArray("DC::true.pid");
	}

}
