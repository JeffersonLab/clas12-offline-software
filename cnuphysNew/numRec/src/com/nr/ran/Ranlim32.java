package com.nr.ran;

/**
 * High-quality random generator using only 32-bit arithmetic. Same conventions
 * as Ran. Period is 3.11E37 . Recommended only when 64-bit arithmetic is not
 * available.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Ranlim32 {
    private int u;
    private int v = 0x85ca18e3; // 0x85ca18e3=2244614371;
    private int w1 = 521288629;
    private int w2 = 362436069;

    public Ranlim32(final int j) {
	u = j ^ v;
	int32();
	v = u;
	int32();
    }

    public int int32() {
	u = u * 0xac564b05 + 1640531513; // 0xac564b05=2891336453
	v ^= v >>> 13;
	v ^= v << 17;
	v ^= v >>> 5;
	w1 = 33378 * (w1 & 0xffff) + (w1 >>> 16);
	w2 = 57225 * (w2 & 0xffff) + (w2 >>> 16);
	int x = u ^ (u << 9);
	x ^= x >>> 17;
	x ^= x << 6;
	int y = w1 ^ (w1 << 17);
	y ^= y >>> 15;
	y ^= y << 5;
	return (x + v) ^ (y + w2);
    }

    public int int32p() {
	return int32() & 0x7FFFFFFF;
    }

    public double doub() {
	double r = 2.32830643653869629E-10 * int32();
	return r < 0 ? 1 + r : r;
    }

    public double truedoub() {
	double r = 2.32830643653869629E-10 * ((int32() & 0xFFFFFFFFL) + 2.32830643653869629E-10 * (int32() & 0xFFFFFFFFL));
	return r;
    }
}
