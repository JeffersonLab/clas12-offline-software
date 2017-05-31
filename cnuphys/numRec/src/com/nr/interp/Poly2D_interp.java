package com.nr.interp;

/**
 * Object for two-dimensional polynomial interpolation on a matrix. Construct
 * with a vector of x1 values, a vector of x2 values, a matrix of tabulated
 * function values yij , and integers to specify the number of points to use
 * locally in each direction. Then call interp for interpolated values.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Poly2D_interp {
    int m, n, mm, nn;
    final double[][] y;
    double[] yv;
    Poly_interp x1terp, x2terp;

    public Poly2D_interp(final double[] x1v, final double[] x2v,
	    final double[][] ym, final int mp, final int np) {

	m = x1v.length;
	n = x2v.length;
	mm = mp;
	nn = np;
	y = ym;
	yv = new double[m];
	x1terp = new Poly_interp(x1v, yv, mm);
	x2terp = new Poly_interp(x2v, x2v, nn);
    }

    public double interp(final double x1p, final double x2p) {
	int i, j, k;
	i = (x1terp.cor != 0) ? x1terp.hunt(x1p) : x1terp.locate(x1p);
	j = (x2terp.cor != 0) ? x2terp.hunt(x2p) : x2terp.locate(x2p);
	for (k = i; k < i + mm; k++) {
	    x2terp.yy = y[k];
	    yv[k] = x2terp.rawinterp(j, x2p);
	}
	return x1terp.rawinterp(i, x1p);
    }
}
