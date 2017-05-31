package com.nr.ran;

/**
 * Recommended generator for everyday use. The period is 1.8E19. Calling
 * conventions same as Ran, above.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Ranq1 {
    private long v = 4101842887655102017L;

    public Ranq1(final long j) {
	v ^= j;
	v = int64();
    }

    public long int64() {
	v ^= v >>> 21;
	v ^= v << 35;
	v ^= v >>> 4;
	return v * 2685821657736338717L;
    }

    /**
     * Return random double-precision floating value in the range 0. to 1.
     * 
     * @return
     */
    public double doub() {
	double r = 5.42101086242752217E-20 * int64();
	return r < 0 ? 1 + r : r;
    }

    public int int32() {
	return (int) int64();
    }

    public int int32p() {
	return int32() & 0x7FFFFFFF;
    }

    public long int64p() {
	return int64() & 0x7FFFFFFFFFFFFFFFL;
    }
}
