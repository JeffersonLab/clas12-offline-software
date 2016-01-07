package cnuphys.ced.event.data;

/**
 * static methods to centralize getting data arrays
 * @author heddle
 *
 */
public class PCAL {

	/**
	 * Get the pid array from the true data
	 * @return the pid array
	 */
	public static int[] pid() {
		return ColumnData.getIntArray("PCAL::true.pid");
	}
	
	/**
	 * Get the sector array from the dgtz array
	 * @return the sector array
	 */
	public static int[] sector() {
		return ColumnData.getIntArray("PCAL::dgtz.sector");
	}
	
	/**
	 * Get the stack array from the dgtz array
	 * @return the stack array (should contain all "1s" for pcal)
	 */
	public static int[] stack() {
		return ColumnData.getIntArray("PCAL::dgtz.stack");
	}
	
	/**
	 * Get the view array from the dgtz array
	 * @return the view array
	 */
	public static int[] view() {
		return ColumnData.getIntArray("PCAL::dgtz.view");
	}

	/**
	 * Get the strip array from the dgtz array
	 * @return the strip array
	 */
	public static int[] strip() {
		return ColumnData.getIntArray("PCAL::dgtz.strip");
	}

	/**
	 * Get the totEdep array from the true data
	 * @return the totEdep array
	 */
	public static double[] totEdep() {
		return ColumnData.getDoubleArray("PCAL::true.totEdep");
	}
	
	
	/**
	 * Get the avgX array from the true data
	 * @return the avgX array
	 */
	public static double[] avgX() {
		return ColumnData.getDoubleArray("PCAL::true.avgX");
	}
	
	/**
	 * Get the avgY array from the true data
	 * @return the avgY array
	 */
	public static double[] avgY() {
		return ColumnData.getDoubleArray("PCAL::true.avgY");
	}
	
	/**
	 * Get the avgZ array from the true data
	 * @return the avgZ array
	 */
	public static double[] avgZ() {
		return ColumnData.getDoubleArray("PCAL::true.avgZ");
	}
	
	/**
	 * Get the avgLx array from the true data
	 * @return the avgLx array
	 */
	public static double[] avgLx() {
		return ColumnData.getDoubleArray("PCAL::true.avgLx");
	}
	
	/**
	 * Get the avgLy array from the true data
	 * @return the avgLy array
	 */
	public static double[] avgLy() {
		return ColumnData.getDoubleArray("PCAL::true.avgLy");
	}
	
	/**
	 * Get the avgLz array from the true data
	 * @return the avgLz array
	 */
	public static double[] avgLz() {
		return ColumnData.getDoubleArray("PCAL::true.avgLz");
	}


	/**
	 * Get the hitn array from the dgtz data
	 * @return the hitn array
	 */
	public static int[] hitn() {
		return ColumnData.getIntArray("PCAL::dgtz.hitn");
	}
	
	/**
	 * Get the ADC array from the dgtz data
	 * @return the ADC array
	 */
	public static int[] ADC() {
		return ColumnData.getIntArray("PCAL::dgtz.ADC");
	}
	
	/**
	 * Get the TDC array from the dgtz data
	 * @return the TDC array
	 */
	public static int[] TDC() {
		return ColumnData.getIntArray("PCAL::dgtz.TDC");
	}

	
	/**
	 * Get the hit count 
	 * @return the hit count
	 */
	public static int hitCount() {
		int sector[] = sector();
		return (sector == null) ? 0 : sector.length;
	}

}
