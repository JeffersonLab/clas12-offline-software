package cnuphys.bCNU.util;

public class Bits {

	/**
	 * See if the control bit is set in the bits variable.
	 * 
	 * @param bits
	 *            the int that holds the bits.
	 * @param b
	 *            the bit to check.
	 * @return <code>true</code> if the bit is set.
	 */
	public static boolean checkBit(int bits, int b) {
		return ((bits & b) == b);
	}

	/**
	 * Sets the given control bit.
	 * 
	 * @param bits
	 *            the int that holds the bits.
	 * @param b
	 *            the bit to set.
	 * @return the modified bits.
	 */
	public static int setBit(int bits, int b) {
		bits |= b;
		return bits;
	}

	/**
	 * Clear the given control bit.
	 * 
	 * @param bits
	 *            the int that holds the bits.
	 * @param b
	 *            the bit to clear.
	 * @return the modified bits.
	 */
	public static int clearBit(int bits, int b) {
		bits &= (~b);
		return bits;
	}

	/**
	 * Toggle the given control bit.
	 * 
	 * @param bits
	 *            the int that holds the bits.
	 * @param b
	 *            the bit to toggle.
	 * @return The modified bits.
	 */
	public static int toggleBit(int bits, int b) {
		bits ^= b;
		return bits;
	}

	/**
	 * Find the first bit set.
	 * 
	 * @return the first bit set, or -1.
	 */
	public static int firstBit(int x) {

		for (int b = 0; x != 0; x = x >> 1) {
			if ((x & 01) == 01) {
				return b;
			}
			b++;
		}

		return -1;
	}

	/**
	 * Count the bits turned on in a word.
	 * 
	 * @param x
	 *            the word to count.
	 * @return the number of "on" bits in the word.
	 */
	public static int countBits(int x) {
		int b;

		for (b = 0; x != 0; x = x >> 1) {
			if ((x & 01) == 01) {
				b++;
			}
		}
		return b;
	}
}
