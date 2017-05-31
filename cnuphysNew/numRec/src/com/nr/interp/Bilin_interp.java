package com.nr.interp;

/**
 * interpolation routines for two dimensions
 * 
 * Object for bilinear interpolation on a matrix. Construct with a vector of x1.
 * values, a vector of x2 values, and a matrix of tabulated function values yij
 * Then call interp for interpolated values.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Bilin_interp {
    int m, n;
    final double[][] y;
    Linear_interp x1terp, x2terp;

    public Bilin_interp(final double[] x1v, final double[] x2v,
	    final double[][] ym) {
	m = x1v.length;
	n = x2v.length;
	y = ym;
	x1terp = new Linear_interp(x1v, x1v);
	x2terp = new Linear_interp(x2v, x2v);
    }

    public double interp(final double x1p, final double x2p) {
	int i, j;
	double yy, t, u;
	i = (x1terp.cor != 0) ? x1terp.hunt(x1p) : x1terp.locate(x1p);
	j = (x2terp.cor != 0) ? x2terp.hunt(x2p) : x2terp.locate(x2p);
	t = (x1p - x1terp.xx[i]) / (x1terp.xx[i + 1] - x1terp.xx[i]);
	u = (x2p - x2terp.xx[j]) / (x2terp.xx[j + 1] - x2terp.xx[j]);
	yy = (1. - t) * (1. - u) * y[i][j] + t * (1. - u) * y[i + 1][j]
		+ (1. - t) * u * y[i][j + 1] + t * u * y[i + 1][j + 1];
	return yy;
    }
}
