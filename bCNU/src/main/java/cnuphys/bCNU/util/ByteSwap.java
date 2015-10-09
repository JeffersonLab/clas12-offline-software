package cnuphys.bCNU.util;

public class ByteSwap {

	/**
	 * Swap 4-byte integer
	 * 
	 * @param val
	 *            integer to swap
	 * @return swapped 4-byte integer
	 */
	public static int swapInt(int val) {

		int temp = 0;

		temp |= (val & 0xff) << 24;
		temp |= (val & 0xff00) << 8;
		temp |= (val & 0xff0000) >>> 8;
		temp |= (val & 0xff000000) >>> 24;
		return temp;
	}

	/**
	 * Swap a 2-byte integer (a short)
	 * 
	 * @param val
	 *            short to swap
	 * @return swapped 2-byte integer (short)
	 */
	public static short swapShort(short val) {

		short temp = 0;

		temp |= (val & 0xff) << 8;
		temp |= (val & 0xff00) >>> 8;
		return temp;
	}

}
