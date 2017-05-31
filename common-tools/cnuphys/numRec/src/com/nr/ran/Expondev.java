package com.nr.ran;

import static java.lang.Math.*;

/**
 * Structure for exponential deviates.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Expondev extends Ran {
    double beta;

    /**
     * Constructor arguments are ˇ and a random sequence seed.
     * 
     * @param bbeta
     * @param i
     */
    public Expondev(final double bbeta, final long i) {
	super(i);
	beta = bbeta;
    }

    /**
     * Return an exponential deviate.
     * 
     * @return
     */
    public double dev() {
	double u;
	do
	    u = doub();
	while (u == 0.);
	return -log(u) / beta;
    }

}
