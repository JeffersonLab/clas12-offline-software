package com.nr.fe;

import static java.lang.Math.*;

/**
 * Convergence acceleration of a sequence by the Levin transformation.
 * Initialize by calling the constructor with arguments nmax, an upper bound on
 * the number of terms to be summed, and epss, the desired accuracy. Then make
 * successive calls to the function next, which returns the current estimate of
 * the limit of the sequence. The flag cnvgd is set when convergence is
 * detected.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Levin {
    private double[] numer, denom;
    private int n, ncv;
    public boolean cnvgd;
    private final double small, eps; // big
    private double lastval, lasteps;

    public Levin(final int nmax, final double epss) {
	numer = new double[nmax];
	denom = new double[nmax];
	n = 0;
	ncv = 0;
	cnvgd = false;
	eps = epss;
	lastval = 0;
	small = Double.MIN_VALUE * 10.0;
	// big=Double.MAX_VALUE;
    }

    public double next(final double sum, final double omega) {
	return next(sum, omega, 1.0);
    }

    /**
     * sum, the nth partial sum of the sequence; omega, the nth remainder
     * estimate wn , usually from (5.3.19); and the parameter beta, which should
     * usually be set to 1, but sometimes 0.5 works better. The current estimate
     * of the limit of the sequence is returned.
     * 
     * @param sum
     * @param omega
     * @param beta
     * @return
     */
    public double next(final double sum, final double omega, final double beta) {
	int j;
	double fact, ratio, term, val;
	term = 1.0 / (beta + n);
	denom[n] = term / omega;
	numer[n] = sum * denom[n];
	if (n > 0) {
	    ratio = (beta + n - 1) * term;
	    for (j = 1; j <= n; j++) {
		fact = (n - j + beta) * term;
		numer[n - j] = numer[n - j + 1] - fact * numer[n - j];
		denom[n - j] = denom[n - j + 1] - fact * denom[n - j];
		term = term * ratio;
	    }
	}
	n++;
	val = abs(denom[0]) < small ? lastval : numer[0] / denom[0];
	lasteps = abs(val - lastval);
	if (lasteps <= eps)
	    ncv++;
	if (ncv >= 2)
	    cnvgd = true;
	return (lastval = val);
    }
}
