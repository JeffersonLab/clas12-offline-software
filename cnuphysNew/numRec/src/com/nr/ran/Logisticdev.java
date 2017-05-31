package com.nr.ran;

import static java.lang.Math.*;

/**
 * Structure for logistic deviates.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Logisticdev extends Ran {
    double mu, sig;

    /**
     * Constructor arguments are mu and sig random sequence seed.
     * 
     * @param mmu
     * @param ssig
     * @param i
     */
    public Logisticdev(final double mmu, final double ssig, final long i) {
	super(i);
	mu = mmu;
	sig = ssig;
    }

    public double dev() {
	double u;
	do
	    u = doub();
	while (u * (1. - u) == 0.);
	return mu + 0.551328895421792050 * sig * log(u / (1. - u));
    }
}
