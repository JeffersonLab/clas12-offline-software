package com.nr.ran;

import static com.nr.NRUtil.*;

/**
 * Cauchy deviates
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Cauchydev extends Ran {
    double mu, sig;

    public Cauchydev(final double mmu, final double ssig, final long i) {
	super(i);
	mu = mmu;
	sig = ssig;
    }

    public double dev() {
	double v1, v2;
	do {
	    v1 = 2.0 * doub() - 1.0;
	    v2 = doub();
	} while (SQR(v1) + SQR(v2) >= 1. || v2 == 0.);
	return mu + sig * v1 / v2;
    }
}
