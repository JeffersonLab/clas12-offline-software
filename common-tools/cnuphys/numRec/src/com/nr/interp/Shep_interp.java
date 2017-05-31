package com.nr.interp;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

/**
 * Object for Shepard interpolation using n points in dim dimensions. Call
 * constructor once, then interp as many times as desired.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Shep_interp {
    int dim, n;
    final double[][] pts;
    final double[] vals;
    double pneg;

    public Shep_interp(final double[][] ptss, final double[] valss) {
	this(ptss, valss, 2.);
    }

    /**
     * The n dim matrix ptss inputs the data points, the vector valss the
     * function values. Set p to the desired exponent. The default value is
     * typical.
     * 
     * @param ptss
     * @param valss
     * @param p
     */
    public Shep_interp(final double[][] ptss, final double[] valss,
	    final double p) {
	dim = ptss[0].length;
	n = ptss.length;
	pts = ptss;
	vals = valss;
	pneg = -p;
    }

    /**
     * Return the interpolated function value at a dim-dimensional point pt.
     * 
     * @param pt
     * @return
     */
    public double interp(final double[] pt) {
	double r, w, sum = 0., sumw = 0.;
	if (pt.length != dim)
	    throw new IllegalArgumentException("RBF_interp bad pt size");
	for (int i = 0; i < n; i++) {
	    if ((r = rad(pt, pts[i])) == 0.)
		return vals[i];
	    sum += (w = pow(r, pneg));
	    sumw += w * vals[i];
	}
	return sumw / sum;
    }

    public double rad(final double[] p1, final double[] p2) {
	double sum = 0.;
	for (int i = 0; i < dim; i++)
	    sum += SQR(p1[i] - p2[i]);
	return sqrt(sum);
    }
}
