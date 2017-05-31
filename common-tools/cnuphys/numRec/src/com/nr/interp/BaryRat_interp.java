package com.nr.interp;

import static java.lang.Math.*;

/**
 * Barycentric rational interpolation object. After constructing the object,
 * call interp for interpolated values. Note that no error estimate dy is
 * calculated.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class BaryRat_interp extends Base_interp {
    double[] w;
    int d;

    /**
     * Constructor arguments are x and y vectors of length n, and order d of
     * desired approximation.
     * 
     * @param xv
     * @param yv
     * @param dd
     */
    public BaryRat_interp(final double[] xv, final double[] yv, final int dd) {
	super(xv, yv, xv.length);
	w = new double[n];
	d = dd;

	if (n <= d)
	    throw new IllegalArgumentException(
		    "d too large for number of points in BaryRat_interp");
	for (int k = 0; k < n; k++) {
	    int imin = max(k - d, 0);
	    int imax = (k >= n - d) ? n - d - 1 : k;
	    double temp = (imin & 1) != 0 ? -1.0 : 1.0;
	    // double temp = imin & 1 ? -1.0 : 1.0;
	    double sum = 0.0;
	    for (int i = imin; i <= imax; i++) {
		int jmax = min(i + d, n - 1);
		double term = 1.0;
		for (int j = i; j <= jmax; j++) {
		    if (j == k)
			continue;
		    term *= (xx[k] - xx[j]);
		}
		term = temp / term;
		temp = -temp;
		sum += term;
	    }
	    w[k] = sum;
	}
    }

    /**
     * Use equation (3.4.9) to compute the barycentric rational interpolant.
     * Note that jl is not used since the approximation is global; it is
     * included only for compatibility with Base_interp.
     * 
     * 
     */
    @Override
    public double rawinterp(final int jl, final double x) {
	double num = 0, den = 0;
	for (int i = 0; i < n; i++) {
	    double h = x - xx[i];
	    if (h == 0.0) {
		return yy[i];
	    } else {
		double temp = w[i] / h;
		num += temp * yy[i];
		den += temp;
	    }
	}
	return num / den;
    }

    /**
     * No need to invoke hunt or locate since the interpolation is global, so
     * override interp to simply call rawinterp directly with a dummy value of
     * jl.
     */
    @Override
    public double interp(final double x) {
	return rawinterp(1, x);
    }
}
