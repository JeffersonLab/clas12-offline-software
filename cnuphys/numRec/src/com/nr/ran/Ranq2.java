package com.nr.ran;

/**
 * Backup generator if Ranq1 has too short a period and Ran is too slow. The
 * period is 8.5E37. Calling conventions same as Ran, above.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Ranq2 {
    private long v = 4101842887655102017L;
    private long w = 1;

    public Ranq2(final long j) {
	v ^= j;
	w = int64();
	v = int64();
    }

    public long int64() {
	v ^= v >>> 17;
	v ^= v << 31;
	v ^= v >>> 8;
	w = 4294957665L * (w & 0xffffffffL) + (w >>> 32);
	return v ^ w;
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
