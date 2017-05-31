package com.nr.ran;

/**
 * Generator for random bytes using the algorithm generally known as RC4.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Ranbyte {
    private int[] s = new int[256];
    private int i, j, ss;
    private int v;

    public Ranbyte(final int u) {
	v = 0x85ca18e3 ^ u; // 0x85ca18e3 = 2244614371L
	for (i = 0; i < 256; i++) {
	    s[i] = i;
	}
	for (j = 0, i = 0; i < 256; i++) {
	    ss = s[i];
	    j = (j + ss + (v >>> 24)) & 0xff;
	    s[i] = s[j];
	    s[j] = ss;
	    v = (v << 24) | (v >>> 8);
	}
	i = j = 0;
	for (int k = 0; k < 256; k++)
	    int8();
    }

    public int int8() {
	i = (i + 1) & 0xff;
	ss = s[i];
	j = (j + ss) & 0xff;
	s[i] = s[j];
	s[j] = ss;
	return (s[(s[i] + s[j]) & 0xff]);
    }

    /**
     * Returns a random 32-bit integer constructed from 4 random bytes. Slow!
     * 
     * @return
     */
    public int int32() {
	v = 0;
	for (int k = 0; k < 4; k++) {
	    i = (i + 1) & 0xff;
	    ss = s[i];
	    j = (j + ss) & 0xff;
	    s[i] = s[j];
	    s[j] = ss;
	    v = (v << 8) | s[(s[i] + s[j]) & 0xff];
	}
	return v;
    }

    public int int32p() {
	return int32() & 0x7FFFFFFF;
    }

    /**
     * Returns a random double-precision floating value between 0. and 1. Slow!!
     * 
     * @return
     */
    public double doub() {
	double r = 2.32830643653869629E-10 * ((int32() & 0xFFFFFFFFL) + 2.32830643653869629E-10 * (int32() & 0xFFFFFFFFL));
	return r;
    }
}
