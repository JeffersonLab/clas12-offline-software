package com.nr.ran;

/**
 * High-quality random hash of an integer into several numeric types.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Ranhash {

    public long int64(final long u) {
	long v = u * 3935559000370003845L + 2691343689449507681L;
	v ^= v >>> 21;
	v ^= v << 37;
	v ^= v >>> 4;
	v *= 4768777513237032717L;
	v ^= v << 20;
	v ^= v >>> 41;
	v ^= v << 5;
	return v;
    }

    public int int32(final long u) {
	return (int) (int64(u) & 0xffffffffL);
    }

    /**
     * Returns hash of u as a double-precision floating value between 0. and 1.
     * 
     * @param u
     * @return
     */
    public double doub(final long u) {
	double r = 5.42101086242752217E-20 * int64(u);
	return r < 0 ? 1 + r : r;
    }

    public int int32p(final long u) {
	return int32(u) & 0x7FFFFFFF;
    }

    public long int64p(final long u) {
	return int64(u) & 0x7FFFFFFFFFFFFFFFL;
    }

    public static void main(final String[] args) {
	Ranhash rh = new Ranhash();
	long u = 1111112L;
	System.out.println(rh.int64(u));
    }
}
