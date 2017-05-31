package com.nr.ran;

/**
 * Implements Knuth's subtractive generator using only floating operations. See
 * text for cautions.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Ranfib {
    private double[] dtab = new double[55];
    private double dd;
    private int inext = 0;
    private int inextp = 31;

    public Ranfib(final long j) {
	Ranq1 init = new Ranq1(j);
	for (int k = 0; k < 55; k++)
	    dtab[k] = init.doub();
    }

    public double doub() {
	if (++inext == 55)
	    inext = 0;
	if (++inextp == 55)
	    inextp = 0;
	dd = dtab[inext] - dtab[inextp];
	if (dd < 0)
	    dd += 1.0;
	return (dtab[inext] = dd);
    }

    /**
     * Returns random 32-bit integer. Recommended only for testing purposes.
     * 
     * @return
     */
    public long int32() {
	return (long) (doub() * 4294967295.0);
    }

    public int int32p() {
	return (int) (int32() & 0x7FFFFFFFL);
    }
}
