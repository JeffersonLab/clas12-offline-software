package cnuphys.snr;

import java.util.Random;
import java.util.StringTokenizer;

public class ExtendedWord {

	// Used for clarity. In JAVA, longs are 64 bits on all machines.
	private static int WORDSIZE = 64;
	
	//for use in hask keys
	private static final String HASH_DELIM = "$";
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
	protected ExtendedWord leftWorkSpace;

	/**
	 * Used for workspace for bleed right
	 */
	protected ExtendedWord rightWorkSpace;

	/**
	 * Creates an extended word made up of an array of longs. Note that in JAVA
	 * a long is always 64 bits, independent of machine.
	 * 
	 * @param bitsNeeded
	 *            the number of bits needed.
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
	 * @param words the words
	 */
	public ExtendedWord(long words[]) {
		this.words = words;
	}

	/**
	 * Set the bit at a given location
	 * 
	 * @param bit
	 *            the bit to set.
	 */
	public void setBit(int bit) {
		int wordIndex = bit / WORDSIZE;
		int b = bit % WORDSIZE;
		words[wordIndex] = setBit(words[wordIndex], b);
	}

	/**
	 * Check if a given bit is on,
	 * 
	 * @param bit
	 *            the bit to check.
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
	 * Perform a bitwise and on two extended words. They are assumed to be of
	 * the same size.
	 * 
	 * @param u
	 *            one of the extended words
	 * @param v
	 *            the other extended word.
	 * @param result
	 *            where the result is stored. Can be u or v.
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
	 * @param u
	 *            one of the extended words
	 * @param v
	 *            the other extended word.
	 * @param result
	 *            where the result is stored. Can be u or v.
	 */
	public static void bitwiseOr(ExtendedWord u, ExtendedWord v, ExtendedWord result) {
		for (int i = 0; i < u.words.length; i++) {
			result.words[i] = u.words[i] | v.words[i];
		}
	}

	/**
	 * Perform a bitwise xor on two extended words. They are assumed to be of
	 * the same size.
	 * 
	 * @param u
	 *            one of the extended words
	 * @param v
	 *            the other extended word.
	 * @param result
	 *            where the result is stored. Can be u or v.
	 */
	public static void bitwiseXor(ExtendedWord u, ExtendedWord v, ExtendedWord result) {
		for (int i = 0; i < u.words.length; i++) {
			result.words[i] = u.words[i] ^ v.words[i];
		}
	}

	/**
	 * Shift the extended word right. CURRENT LIMITATION: can only shift up to
	 * 64 bits.
	 * 
	 * @param n
	 *            the number of places to shift.
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
	 * Shift the extended word left. CURRENT LIMITATION: can only shift up to 64
	 * bits.
	 * 
	 * @param n
	 *            the number of places to shift.
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
	 * @param n
	 *            the number of bits to bleed left.
	 */
	public synchronized void bleedRight(int n) {
		if (rightWorkSpace == null) {
			rightWorkSpace = new ExtendedWord();
			rightWorkSpace.words = new long[words.length];
		}

		ExtendedWord.copy(this, rightWorkSpace);

		if (n < 4) {
			for (int j = 0; j < n; j++) {
				rightWorkSpace.shiftRight(1);
				ExtendedWord.bitwiseOr(this, rightWorkSpace, this);
			}
			return;
		}

		int m = (n + 1) / 2;
		int k = n - m;

		for (int j = 0; j < m; j++) {
			rightWorkSpace.shiftRight(1);
			ExtendedWord.bitwiseOr(this, rightWorkSpace, this);
		}

		ExtendedWord.copy(this, rightWorkSpace);
		rightWorkSpace.shiftRight(k);
		ExtendedWord.bitwiseOr(this, rightWorkSpace, this);

	}

	/**
	 * Bleed the specified number of bits left.
	 * 
	 * @param n
	 *            the number of bits to bleed left.
	 */
	public synchronized void bleedLeft(int n) {
		if (leftWorkSpace == null) {
			leftWorkSpace = new ExtendedWord();
			leftWorkSpace.words = new long[words.length];
		}

		ExtendedWord.copy(this, leftWorkSpace);

		if (n < 4) {
			for (int j = 0; j < n; j++) {
				leftWorkSpace.shiftLeft(1);
				ExtendedWord.bitwiseOr(this, leftWorkSpace, this);
			}
			return;
		}

		int m = (n + 1) / 2;
		int k = n - m;

		for (int j = 0; j < m; j++) {
			leftWorkSpace.shiftLeft(1);
			ExtendedWord.bitwiseOr(this, leftWorkSpace, this);
		}

		ExtendedWord.copy(this, leftWorkSpace);
		leftWorkSpace.shiftLeft(k);
		ExtendedWord.bitwiseOr(this, leftWorkSpace, this);
	}

	/**
	 * Create a binary string representation.
	 * 
	 * @return
	 */
	public String binaryString() {
		StringBuffer sb = new StringBuffer(WORDSIZE * words.length);
		for (int i = words.length - 1; i >= 0; i--) {
			sb.append(binaryString(words[i]));
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
	 * @param b
	 *            the byte in question. Sample results
	 *            <p>
	 *            b = 17 -> 00010001
	 *            <p>
	 *            b = 127 -> 01111111
	 *            <p>
	 *            b = 128 (-128) -> 10000000
	 *            <p>
	 *            b = 129 (-127) -> 10000001
	 *            <p>
	 *            b = 255 (-1) -> 11111111
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
	 * @param word
	 *            the long that holds the bits.
	 * @param _b
	 *            the bit to check.
	 * @return <code>true</code> if the bit is set.
	 */
	private static boolean checkBit(long word, int bit) {
		long mask = 1L << bit;
		return ((word & mask) == mask);
	}

	/**
	 * setBit sets the given control bit.
	 * 
	 * @param bits
	 *            The long that holds the bits.
	 * @param bit
	 *            the bit to set.
	 * @return The modified bits.
	 */
	private static long setBit(long bits, int bit) {
		bits |= (1L << bit);
		return bits;
	}


	/**
	 * Get the underlying words
	 * @return the words
	 */
	public long[] getWords() {
		return words;
	}
	
	public static int countCommonBits(ExtendedWord a, ExtendedWord b, ExtendedWord work1, ExtendedWord work2, ExtendedWord work3) {

		bitwiseAnd(a, b, work1);
		a.negate();
		b.negate();
		bitwiseAnd(a, b, work2);
		
		bitwiseOr(work1, work2, work3);
		
		//restore
		a.negate();
		b.negate();

		return work3.bitCount();
	}
	

	/**
	 * Hash this ExtendedWord into a String
	 * @return a String suitable as a hash or map key
	 */
	public String hashKey() {
		StringBuilder sb = new StringBuilder(128);

		for (long word : getWords()) {
			if (sb.length() > 0) {
				sb.append(HASH_DELIM);
			}
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
	 * Convert back to an ExtendedWord from a hash key
	 * @param hash the key
	 * @return the equivaleny ExtendedWord
	 */
	public static ExtendedWord fromHash(String hash) {
		StringTokenizer t = new StringTokenizer(hash, HASH_DELIM);
		int num = t.countTokens();
		
		long[] words = new long[num];
		for (int i = 0; i < num; i++) {
			words[i] = Long.valueOf(t.nextToken(), HASHRADIX);
		}
		
		return new ExtendedWord(words);
	}
	

	public static void main(String arg[]) {
		ExtendedWord a = new ExtendedWord(112);
		ExtendedWord b = new ExtendedWord(112);
		
		Random rand = new Random();
		a.words[0] = rand.nextLong();
		a.words[1] = rand.nextLong();
		b.words[0] = rand.nextLong();
		b.words[1] = rand.nextLong();
		
		String ahash = a.hashKey();
		String bhash = b.hashKey();
		
//		System.err.println("" + a);
		System.err.println("ahash [" + ahash + "]");
		System.err.println("bhash [" + bhash + "]");
		
		ExtendedWord ap = fromHash(ahash);
		ExtendedWord bp = fromHash(bhash);
		
		System.err.println(" a = " + a);
		System.err.println("ap = " + ap);
		System.err.println(" b = " + b);
		System.err.println("bp = " + bp);
		
		
		
	}

}
