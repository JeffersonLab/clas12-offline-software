package com.nr.interp;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.la.LUdcmp;

/**
 * Object for radial basis function interpolation using n points in dim
 * dimensions. Call constructor once, then interp as many times as desired.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class RBF_interp {
    int dim, n;
    final double[][] pts;
    final double[] vals;
    double[] w;
    RBF_fn fn;
    boolean norm;

    public RBF_interp(final double[][] ptss, final double[] valss,
	    final RBF_fn func) {
	this(ptss, valss, func, false);
    }

    /**
     * The n dim matrix ptss inputs the data points, the vector valss the
     * function values. func contains the chosen radial basis function, derived
     * from the class RBF_fn. The default value of nrbf gives RBF interpolation;
     * set it to 1 for NRBF.
     * 
     * @param ptss
     * @param valss
     * @param func
     * @param nrbf
     */
    public RBF_interp(final double[][] ptss, final double[] valss,
	    final RBF_fn func, final boolean nrbf) {
	dim = ptss[0].length;
	n = ptss.length;
	pts = ptss;
	vals = valss;
	w = new double[n];
	fn = func;
	norm = nrbf;

	int i, j;
	double sum;
	double[][] rbf = new double[n][n];
	double[] rhs = new double[n];
	for (i = 0; i < n; i++) {
	    sum = 0.;
	    for (j = 0; j < n; j++) {
		sum += (rbf[i][j] = fn.rbf(rad(pts[i], pts[j])));
	    }
	    if (norm)
		rhs[i] = sum * vals[i];
	    else
		rhs[i] = vals[i];
	}
	LUdcmp lu = new LUdcmp(rbf);
	lu.solve(rhs, w);
    }

    /**
     * Return the interpolated function value at a dim-dimensional point pt.
     * 
     * @param pt
     * @return
     */
    public double interp(final double[] pt) {
	double fval, sum = 0., sumw = 0.;
	if (pt.length != dim)
	    throw new IllegalArgumentException("RBF_interp bad pt size");
	for (int i = 0; i < n; i++) {
	    fval = fn.rbf(rad(pt, pts[i]));
	    sumw += w[i] * fval;
	    sum += fval;
	}
	return norm ? sumw / sum : sumw;
    }

    public double rad(final double[] p1, final double[] p2) {
	double sum = 0.;
	for (int i = 0; i < dim; i++)
	    sum += SQR(p1[i] - p2[i]);
	return sqrt(sum);
    }
}
