package com.nr.root;

import static java.lang.Math.*;
import com.nr.la.LUdcmp;

/**
 * Newton-Raphson Method for Nonlinear Systems of Equations multidimensional
 * Newton root finding Copyright (C) Numerical Recipes Software 1986-2007 Java
 * translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public abstract class MNEWT {

    public MNEWT() {
    }

    public abstract void funk(double[] x, double[] fvec, double[][] fjac);

    /**
     * Given an initial guess x[0..n-1] for a root in n dimensions, take ntrial
     * Newton-Raphson steps to improve the root. Stop if the root converges in
     * either summed absolute variable increments tolx or summed absolute
     * function values tolf.
     * 
     * @param ntrial
     * @param x
     * @param tolx
     * @param tolf
     */
    public void mnewt(final int ntrial, final double[] x, final double tolx,
	    final double tolf) {
	int i, n = x.length;
	double[] p = new double[n], fvec = new double[n];
	double[][] fjac = new double[n][n];
	for (int k = 0; k < ntrial; k++) {
	    funk(x, fvec, fjac);
	    double errf = 0.0;
	    for (i = 0; i < n; i++)
		errf += abs(fvec[i]);
	    if (errf <= tolf)
		return;
	    for (i = 0; i < n; i++)
		p[i] = -fvec[i];
	    LUdcmp alu = new LUdcmp(fjac);
	    alu.solve(p, p);
	    double errx = 0.0;
	    for (i = 0; i < n; i++) {
		errx += abs(p[i]);
		x[i] += p[i];
	    }
	    if (errx <= tolx)
		return;
	}
	return;
    }
}
