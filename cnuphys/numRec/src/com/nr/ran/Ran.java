package com.nr.ran;

/**
 * Implementation of the highest quality recommended generator. The constructor
 * is called with an integer seed and creates an instance of the generator. The
 * member functions int64, doub, and int32 return the next values in the random
 * sequence, as a variable type indicated by their names. The period of the
 * generator is 3.138E57.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Ran {
    private long u;
    private long v = 4101842887655102017L;
    private long w = 1;

    /**
     * Call with any integer seed (except value of v above).
     * 
     * @param j
     */
    public Ran(final long j) {
	u = j ^ v;
	int64();
	v = u;
	int64();
	w = v;
	int64();
    }

    /**
     * Return 64-bit random integer. See text for explanation of method.
     * 
     * @return
     */
    public long int64() {
	u = u * 2862933555777941757L + 7046029254386353087L;
	v ^= v >>> 17;
	v ^= v << 31;
	v ^= v >>> 8;
	w = 4294957665L * (w & 0xffffffffL) + (w >>> 32);
	long x = u ^ (u << 21);
	x ^= x >>> 35;
	x ^= x << 4;
	return (x + v) ^ w;
    }

    /**
     * Return random double-precision floating value in the range 0. to 1.
     * 
     * @return
     */
    public double doub() { // XXX as int64 is signed.
	double r = 5.42101086242752217E-20 * int64();
	return r < 0 ? 1 + r : r;
    }

    /**
     * Return 32-bit random integer.
     * 
     * @return
     */
    public int int32() {
	return (int) int64();
    }

    public int int32p() {
	return int32() & 0x7FFFFFFF;
    }

    public long int64p() {
	return int64() & 0x7FFFFFFFFFFFFFFFL;
    }

    public static void main(String[] args) {
	Ran ran = new Ran(17);
	long r = ran.int64();
	System.out.printf("%x\n", r);
    }

}
