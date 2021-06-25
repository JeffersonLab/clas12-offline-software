package cnuphys.snr;

import java.util.Random;
import java.util.StringTokenizer;

public class ExtendedWord {

	// Used for clarity. In JAVA, longs are 64 bits on all machines.
	private static int WORDSIZE = 64;

	// for use in hask keys
	private static final String HASH_DELIM = "$";
	private static final int HASHRADIX = 36;

	// Word with all bits on
	private static final long ALLBITSON = 0xFFFFFFFFFFFFFFFFL;

	// for base62 encoding
	private static final char[] digits = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	private static final int RADIX62 = 62;
	private static final long MAX62 = Long.MAX_VALUE / RADIX62;

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
	 * Bleed the specified number of bits right.
	 * 
	 * @param n the number of bits to bleed right.
	 */
	public synchronized void OLDbleedRight(int n) {
//		if (rightWorkSpace == null) {
//			rightWorkSpace = new ExtendedWord();
//			rightWorkSpace.words = new long[words.length];
//		}
//
//		ExtendedWord.copy(this, rightWorkSpace);
//
//		if (n < 4) {
//			for (int j = 0; j < n; j++) {
//				rightWorkSpace.shiftRight(1);
//				ExtendedWord.bitwiseOr(this, rightWorkSpace, this);
//			}
//			return;
//		}
//
//		int m = (n + 1) / 2;
//		int k = n - m;
//
//		for (int j = 0; j < m; j++) {
//			rightWorkSpace.shiftRight(1);
//			ExtendedWord.bitwiseOr(this, rightWorkSpace, this);
//		}
//
//		ExtendedWord.copy(this, rightWorkSpace);
//		rightWorkSpace.shiftRight(k);
//		ExtendedWord.bitwiseOr(this, rightWorkSpace, this);

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
	 * Bleed the specified number of bits left.
	 * 
	 * @param n the number of bits to bleed left.
	 */
	public synchronized void OLDbleedLeft(int n) {
//		if (leftWorkSpace == null) {
//			leftWorkSpace = new ExtendedWord();
//			leftWorkSpace.words = new long[words.length];
//		}
//
//		ExtendedWord.copy(this, leftWorkSpace);
//
//		if (n < 4) {
//			for (int j = 0; j < n; j++) {
//				leftWorkSpace.shiftLeft(1);
//				ExtendedWord.bitwiseOr(this, leftWorkSpace, this);
//			}
//			return;
//		}
//
//		int m = (n + 1) / 2;
//		int k = n - m;
//
//		for (int j = 0; j < m; j++) {
//			leftWorkSpace.shiftLeft(1);
//			ExtendedWord.bitwiseOr(this, leftWorkSpace, this);
//		}
//
//		ExtendedWord.copy(this, leftWorkSpace);
//		leftWorkSpace.shiftLeft(k);
//		ExtendedWord.bitwiseOr(this, leftWorkSpace, this);
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

	public static int countCommonBits(ExtendedWord a, ExtendedWord b, ExtendedWord work1, ExtendedWord work2,
			ExtendedWord work3) {

		bitwiseAnd(a, b, work1);
		a.negate();
		b.negate();
		bitwiseAnd(a, b, work2);

		bitwiseOr(work1, work2, work3);

		// restore
		a.negate();
		b.negate();

		return work3.bitCount();
	}

	/**
	 * Hash this ExtendedWord into a String
	 * 
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
	 * Hash this ExtendedWord into a String in base 62
	 * 
	 * @return a String suitable as a hash or map key
	 */
	public String hashKey62() {
		StringBuilder sb = new StringBuilder(128);

		for (long word : getWords()) {
			if (sb.length() > 0) {
				sb.append(HASH_DELIM);
			}
			if (word != 0) {
				String s62 = encode62(word);
				sb.append(s62);
			} else {
				sb.append('0');
			}
		}

		return sb.toString();
	}

	/**
	 * Convert back to an ExtendedWord from a hash key
	 * 
	 * @param hash the key
	 * @return the equivalent ExtendedWord
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

	/**
	 * Convert back to an ExtendedWord from a hash key
	 * 
	 * @param hash the key
	 * @return the equivalent ExtendedWord
	 */
	public static ExtendedWord fromHash62(String hash) {
		StringTokenizer t = new StringTokenizer(hash, HASH_DELIM);
		int num = t.countTokens();

		long[] words = new long[num];
		for (int i = 0; i < num; i++) {
			words[i] = parseLong62(t.nextToken());
		}

		return new ExtendedWord(words);
	}

	/**
	 * Helper for parsing longs.
	 *
	 * @param str the string to parse
	 * @return the parsed long value
	 * @throws NumberFormatException if there is an error
	 * @throws NullPointerException  if decode is true and str is null
	 * @see #parseLong(String, int)
	 * @see #decode(String)
	 */
	private static long parseLong62(String str) {
		if (str == null) {
			throw new NumberFormatException();
		}

		int index = 0;
		int len = str.length();
		boolean isNeg = false;

		if (len == 0) {
			throw new NumberFormatException();
		}

		int ch = str.charAt(index);
		if (ch == '-') {
			if (len == 1)
				throw new NumberFormatException();

			isNeg = true;
			ch = str.charAt(++index);
		}

		if (index == len) {
			throw new NumberFormatException();
		}

		long val = 0;
		while (index < len) {
			if (val < 0 || val > MAX62)
				throw new NumberFormatException();

			ch = digit62(str.charAt(index++));
			val = val * RADIX62 + ch;
			if (ch < 0 || (val < 0 && (!isNeg || val != Long.MIN_VALUE)))
				throw new NumberFormatException();
		}
		return isNeg ? -val : val;
	}

	// get the value of a digit for base 62
	private static int digit62(char ch) {
		if (Character.isDigit(ch)) {
			return ch - '0';
		}

		else if (Character.isLowerCase(ch)) {
			return 10 + ch - 'a';
		}

		return 36 + ch - 'A';
	}

	/**
	 * Encode a long into a base 62 String. Based on the toString implementation in
	 * the Java Long class.
	 * 
	 * @param number the number to encode
	 * @return a base 62 number
	 */
	public static String encode62(long num) {

		// For negative numbers, print out the absolute value w/ a leading '-'.
		// Use an array large enough for a binary number.
		char[] buffer = new char[65];
		int i = 65;
		boolean isNeg = false;
		if (num < 0) {
			isNeg = true;
			num = -num;

			// When the value is MIN_VALUE, it overflows when made positive
			if (num < 0) {
				buffer[--i] = digits[(int) (-(num + RADIX62) % RADIX62)];
				num = -(num / RADIX62);
			}
		}

		do {
			buffer[--i] = digits[(int) (num % RADIX62)];
			num /= RADIX62;
		} while (num > 0);

		if (isNeg)
			buffer[--i] = '-';

		// Package constructor avoids an array copy.
		return new String(buffer, i, 65 - i);

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

	//test the hashing
	private static void hashTest() {
		Random rand = new Random();
		ExtendedWord a = randomWord(112, rand, 0.5f);
		ExtendedWord b = randomWord(112, rand, 0.5f);

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

		System.err.println("\nNow try base 62");
//		System.err.println("digitsChar62[0] = " + digitsChar62[61]);

		String ahash62 = a.hashKey62();
		String bhash62 = b.hashKey62();
		System.err.println("ahash62 [" + ahash62 + "]");
		System.err.println("bhash62 [" + bhash62 + "]");

		ExtendedWord a62 = fromHash62(ahash62);
		ExtendedWord b62 = fromHash62(bhash62);

		System.err.println("  a = " + a);
		System.err.println("a62 = " + a62);
		System.err.println("  b = " + b);
		System.err.println("b62 = " + b62);
	}
	
	private static void bleedTest() {
//		Random rand = new Random();
//		ExtendedWord ew = randomWord(112, rand, 0.1f);
//		
//		int bleedAmount = 5;
//		
//		ExtendedWord ewCopy = new ExtendedWord(112);
//		copy(ew, ewCopy);
//
////		System.err.println("     ew = " + ew);
//		ew.bleedLeft(bleedAmount);
////		System.err.println("    *ew = " + ew);
//
////		System.err.println(" ewCopy = " + ewCopy);
//		ewCopy.newBleedLeft(bleedAmount);
////		System.err.println("*ewCopy = " + ewCopy);
//		
//		if (ew.equals(ewCopy)) {
//	//		System.err.println("The two results are equal");
//		}
//		else {
//			System.err.println("ERROR The two results are not equal");	
//			System.exit(1);
//		}
		
	}
	
	private static void bleedTimingTest() {
//		//prime pump
//		
//		for (int i = 0; i < 1000; i++) {
//			bleedTest();
//		}
//		
//		long seed = 34635591;
//		int bleedAmount = 5;
//		int num = 1000000;
//		float hitProb = 0.1f;
//		
//		ExtendedWord ewOld[] = new ExtendedWord[num];
//		ExtendedWord ewNew[] = new ExtendedWord[num];
//
//		
//		//create words
//		Random rand = new Random(seed);
//		for (int i = 0; i < num; i++) {
//			ewOld[i] = randomWord(112, rand, hitProb);
//			ewNew[i] = new ExtendedWord(112);
//			copy(ewOld[i], ewNew[i]);
//		}
//		
//		long time = System.currentTimeMillis();
//		
//		for (int i = 0; i < num; i++) {
//			ewNew[i].newBleedRight(bleedAmount);
//		}
//		
//		System.err.println("New Time: " + (System.currentTimeMillis() - time));
//
//		time = System.currentTimeMillis();
//		
//		for (int i = 0; i < num; i++) {
//			ewOld[i].bleedRight(bleedAmount);
//		}
//		
//		System.err.println("Old Time: " + (System.currentTimeMillis() - time));
//		
//		
//		
//		//equatity check
//		for (int i = 0; i < num; i++) {
//			if (!ewOld[i].equals(ewNew[i])) {
//				System.err.println("Equality test FAILED for i = " + i);
//			}
//		}
//
//		System.err.println("Passed timing equality test");
	}
	
	/**
	 * Main program for testing
	 * @param arg
	 */
	public static void main(String arg[]) {
		// hashTest();

		int num = 100000;
		for (int i = 0; i < num; i++) {
			bleedTest();
		}
		System.err.println("Tested " + num + " successfully");
		
		bleedTimingTest();
	}

}
