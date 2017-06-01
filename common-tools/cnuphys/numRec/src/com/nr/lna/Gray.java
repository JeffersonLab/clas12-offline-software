package com.nr.lna;

/**
 * Gray Codes
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Gray {

    public int gray(final int n) {
	return n ^ (n >>> 1);
    }

    public int invgray(final int n) {
	int ish = 1;
	int ans = n, idiv;
	for (;;) {
	    ans ^= (idiv = ans >>> ish);
	    if (idiv <= 1 || ish == 16)
		return ans;
	    ish <<= 1;
	}
    }
}
