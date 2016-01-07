package cnuphys.ced.event.data;

/**
 * static methods to centralize getting data arrays
 * @author heddle
 *
 */
public class FTOF {

	/**
	 * Get the pid 1A array from the true data
	 * @return the pid 1A array
	 */
	public static int[] pid_1A() {
		return ColumnData.getIntArray("FTOF1A::true.pid");
	}
	
	/**
	 * Get the pid 1B array from the true data
	 * @return the pid 1B array
	 */
	public static int[] pid_1B() {
		return ColumnData.getIntArray("FTOF1B::true.pid");
	}

	/**
	 * Get the pid 2B array from the true data
	 * @return the pid 2B array
	 */
	public static int[] pid_2B() {
		return ColumnData.getIntArray("FTOF2B::true.pid");
	}

}
