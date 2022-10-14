package cnuphys.snr;

import java.util.Random;

public class ExtendedWord {

	// Used for clarity. In JAVA, longs are 64 bits on all machines.
	private static int WORDSIZE = 64;

	private static final int HASHRADIX = 36;

	// Word with all bits on
	private static final long ALLBITSON = 0xFFFFFFFFFFFFFFFFL;

	/**
	 * Holds the composite words. words[0] is least significant.
	 */
	protected long words[];

	/**
	 * Used for workspace for bleed left
	 */
//	protected ExtendedWord leftWorkSpace;

	/**
	 * Used for workspace for bleed right
	 */
//	protected ExtendedWord rightWorkSpace;

	/**
	 * Creates an extended word made up of an array of longs. Note that in JAVA a
	 * long is always 64 bits, independent of machine.
	 *
	 * @param bitsNeeded the number of bits needed.
	 */
	public ExtendedWord(int bitsNeeded) {
		int n = 1 + (bitsNeeded - 1) / WORDSIZE;
		words = new long[n];
	}

	/**
	 * Creates an empty extended word.
	 */
	public ExtendedWord() {
	}

	/**
	 * Create using words
	 *
	 * @param words the words
	 */
	public ExtendedWord(long words[]) {
		this.words = words;
	}

	/**
	 * Test for equality with another word
	 * @param ew the test word
	 * @return <code>true</code> if the two words have the same value
	 */
	public boolean equals(ExtendedWord ew) {
		if (words.length != ew.words.length) {
			return false;
		}

		for (int i = 0; i < words.length; i++) {
			if (words[i] != ew.words[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Set the bit at a given location
	 *
	 * @param bit the bit to set.
	 */
	public void setBit(int bit) {
		int wordIndex = bit / WORDSIZE;
		int b = bit % WORDSIZE;
		words[wordIndex] = setBit(words[wordIndex], b);
	}

	/**
	 * Clear the bit at a given location
	 *
	 * @param bit the bit to clear.
	 */
	public void clearBit(int bit) {
		int wordIndex = bit / WORDSIZE;
		int b = bit % WORDSIZE;
		words[wordIndex] = clearBit(words[wordIndex], b);
	}


	/**
	 * Check if a given bit is on,
	 *
	 * @param bit the bit to check.
	 * @return <code>true</code> if the bit is set.
	 */
	public boolean checkBit(int bit) {
		int wordIndex = bit / WORDSIZE;
		int b = bit % WORDSIZE;
		return checkBit(words[wordIndex], b);
	}

	/**
	 * Zero all the bits
	 */
	public void clear() {
		for (int i = 0; i < words.length; i++) {
			words[i] = 0;
		}
	}

	/**
	 * Set all bits to 1
	 */
	public void fill() {
		for (int i = 0; i < words.length; i++) {
			words[i] = ALLBITSON;
		}
	}

	/**
	 * See if this extended word is zero
	 *
	 * @return <code>if this extended word is zero
	 */
	public boolean isZero() {
		for (long lw : words) {
			if (lw != 0L) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Count all the on bits in the extended word.
	 *
	 * @return count of all the on bits in the extended word.
	 */
	public int bitCount() {
		int count = 0;
		for (long lw : words) {
			count += Long.bitCount(lw);
			// count += bitCount(lw);
		}

		return count;
	}

	/**
	 * Copy value from one extended word to another.
	 *
	 * @param src
	 * @param dest
	 */
	public static void copy(ExtendedWord src, ExtendedWord dest) {
		for (int i = 0; i < src.words.length; i++) {
			dest.words[i] = src.words[i];
		}
	}

	/**
	 * Toggle all the bits in an ExtendedWord.
	 */
	public void negate() {
		for (int i = 0; i < words.length; i++) {
			words[i] = ~words[i];
		}
	}

	/**
	 * Perform a bitwise and on two extended words. They are assumed to be of the
	 * same size.
	 *
	 * @param u      one of the extended words
	 * @param v      the other extended word.
	 * @param result where the result is stored. Can be u or v.
	 */
	public static void bitwiseAnd(ExtendedWord u, ExtendedWord v, ExtendedWord result) {
		for (int i = 0; i < u.words.length; i++) {
			result.words[i] = u.words[i] & v.words[i];
		}
	}

	/**
	 * Perform a bitwise or on two extended words. They are assumed to be of the
	 * same size.
	 *
	 * @param u      one of the extended words
	 * @param v      the other extended word.
	 * @param result where the result is stored. Can be u or v.
	 */
	public static void bitwiseOr(ExtendedWord u, ExtendedWord v, ExtendedWord result) {
		for (int i = 0; i < u.words.length; i++) {
			result.words[i] = u.words[i] | v.words[i];
		}
	}

	/**
	 * Perform a bitwise xor on two extended words. They are assumed to be of the
	 * same size.
	 *
	 * @param u      one of the extended words
	 * @param v      the other extended word.
	 * @param result where the result is stored. Can be u or v.
	 */
	public static void bitwiseXor(ExtendedWord u, ExtendedWord v, ExtendedWord result) {
		for (int i = 0; i < u.words.length; i++) {
			result.words[i] = u.words[i] ^ v.words[i];
		}
	}

	/**
	 * Shift the extended word right. CURRENT LIMITATION: can only shift up to 64
	 * bits.
	 *
	 * @param n the number of places to shift.
	 */
	public void shiftRight(int n) {
		int len = words.length;
		int nm1 = len - 1;

		int complementShift = WORDSIZE - n;

		for (int i = 0; i < len; i++) {
			words[i] >>>= n;

			/* now must account for boundaries, except for last word */

			if (i < nm1) {
				long temp = words[i + 1] << complementShift;
				words[i] |= temp;
			}
		}
	}

	/**
	 * Shift the extended word right and or with self.
	 * CURRENT LIMITATION: can only shift up to 64
	 * bits.
	 *
	 * @param n the number of places to shift.
	 */
	public ExtendedWord shiftRightAndOr(int n) {
		int len = words.length;
		int nm1 = len - 1;

		int complementShift = WORDSIZE - n;

		for (int i = 0; i < len; i++) {
			words[i] = words[i] | (words[i] >>>= n);

			/* now must account for boundaries, except for last word */

			if (i < nm1) {
				long temp = words[i + 1] << complementShift;
				words[i] |= temp;
			}
		}

		return this;
	}


	/**
	 * Shift the extended word left and or with self.
	 * CURRENT LIMITATION: can only shift up to 64
	 * bits.
	 *
	 * @param n the number of places to shift.
	 */
	public ExtendedWord shiftLeftAndOr(int n) {
		int len = words.length;
		int nm1 = len - 1;

		int complementShift = WORDSIZE - n;

		for (int i = nm1; i >= 0; i--) {
			words[i] = words[i] | (words[i] << n);

			/* now must account for boundaries, except for last word */

			if (i >0) {
				long temp = words[i - 1] >>> complementShift;
				words[i] |= temp;
			}
		}

		return this;
	}

	/**
	 * Shift the extended word left. CURRENT LIMITATION: can only shift up to 64
	 * bits.
	 *
	 * @param n the number of places to shift.
	 */
	public void shiftLeft(int n) {
		int len = words.length;
		int nm1 = len - 1;

		int complementShift = WORDSIZE - n;

		for (int i = nm1; i >= 0; i--) {
			words[i] <<= n;

			/* now must account for boundaries, except for last word */

			if (i > 0) {
				long temp = words[i - 1] >>> complementShift;
				words[i] |= temp;
			}
		}
	}

	/**
	 * Bleed the specified number of bits left.
	 *
	 * @param n the number of bits to bleed left.
	 */
	public synchronized void bleedLeft(int n) {

		int bleedAmount = 1;
		while (n > 0) {
			shiftLeftAndOr(bleedAmount);
			n = n - bleedAmount;

			bleedAmount = Integer.min(n, 2*bleedAmount);

		}
	}

	/**
	 * Bleed the specified number of bits right.
	 *
	 * @param n the number of bits to bleed right.
	 */
	public synchronized void bleedRight(int n) {

		int bleedAmount = 1;
		while (n > 0) {
			shiftRightAndOr(bleedAmount);
			n = n - bleedAmount;

			bleedAmount = Integer.min(n, 2*bleedAmount);

		}
	}


	/**
	 * Create a binary string representation.
	 *
	 * @return a binary string representation
	 */
	public String binaryString() {
		StringBuffer sb = new StringBuffer(WORDSIZE * words.length);
		for (int i = words.length - 1; i >= 0; i--) {
			sb.append(binaryString(words[i]));
			sb.append(" ");
		}
		return sb.toString();
	}

	/**
	 * Create a string representation.
	 *
	 * @return a string representation.
	 */
	@Override
	public String toString() {
		return binaryString();
	}

	/**
	 * Return a binary string representation of a byte.
	 *
	 * @param b the byte in question. Sample results
	 *          <p>
	 *          b = 17 -> 00010001
	 *          <p>
	 *          b = 127 -> 01111111
	 *          <p>
	 *          b = 128 (-128) -> 10000000
	 *          <p>
	 *          b = 129 (-127) -> 10000001
	 *          <p>
	 *          b = 255 (-1) -> 11111111
	 * @return a binary string representation.
	 */
	private static String binaryString(long b) {
		StringBuffer sb = new StringBuffer(WORDSIZE);
		for (int i = 0; i < WORDSIZE; i++) {
			if ((b & 1) == 1) {
				sb.append('1');
			} else {
				sb.append('0');
			}

			b = b >>> 1;
		}
		sb.reverse();
		return sb.toString();
	}

	/**
	 * checkBit returns true if the control bit is set in the bits variable.
	 *
	 * @param word the long that holds the bits.
	 * @param _b   the bit to check.
	 * @return <code>true</code> if the bit is set.
	 */
	private static boolean checkBit(long word, int bit) {
		long mask = 1L << bit;
		return ((word & mask) == mask);
	}

	/**
	 * setBit sets the given control bit.
	 *
	 * @param bits The long that holds the bits.
	 * @param bit  the bit to set.
	 * @return The modified bits.
	 */
	private static long setBit(long bits, int bit) {
		bits |= (1L << bit);
		return bits;
	}

	/**
	 * clearBit clears the given control bit.
	 *
	 * @param bits The long that holds the bits.
	 * @param bit  the bit to clear.
	 * @return The modified bits.
	 */
	private static long clearBit(long bits, int bit) {
		bits &= ~(1L << bit);
		return bits;
	}


	/**
	 * Get the underlying words
	 *
	 * @return the words
	 */
	public long[] getWords() {
		return words;
	}


	/**
	 * Hash this ExtendedWord into a String
	 *
	 * @return a String suitable as a hash or map key
	 */
	public String hashKey() {
		StringBuilder sb = new StringBuilder(128);

		for (long word : getWords()) {
			if (word != 0) {
				String hexStr = Long.toString(word, HASHRADIX);
				sb.append(hexStr);
			} else {
				sb.append('0');
			}
		}

		return sb.toString();
	}


	/**
	 * Create a random word
	 * @param bitsNeeded the number of bits needeed
	 * @param rand the random number generator
	 * @param setProb the probability that w bit will be set
	 * @return a random word
	 */
	public static ExtendedWord randomWord(int bitsNeeded, Random rand, float setProb) {
		ExtendedWord ew = new ExtendedWord(bitsNeeded);
		for (int bit = 0; bit < WORDSIZE * ew.words.length; bit++) {
			float nextRand = rand.nextFloat();
			if (nextRand < setProb) {
				ew.setBit(bit);
			}
		}
	    return ew;
	}


}
