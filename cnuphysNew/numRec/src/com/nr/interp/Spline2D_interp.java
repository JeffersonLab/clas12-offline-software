package com.nr.interp;

import static com.nr.NRUtil.*;

import org.netlib.util.doubleW;

/**
 * 
 * Object for two-dimensional cubic spline interpolation on a matrix. Construct
 * with a vector of x1 values, a vector of x2 values, and a matrix of tabulated
 * function values yij. Then call interp for interpolated values.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Spline2D_interp {
    int m, n;
    final double[][] y;
    final double[] x1;
    double[] yv;
    Spline_interp[] srp;

    // NRvector<Spline_interp*> srp;

    public Spline2D_interp(final double[] x1v, final double[] x2v,
	    final double[][] ym) {
	m = x1v.length;
	n = x2v.length;
	y = ym;
	yv = new double[m];
	x1 = x1v;
	srp = new Spline_interp[m];
	for (int i = 0; i < m; i++)
	    srp[i] = new Spline_interp(x2v, y[i]);
    }

    public double interp(final double x1p, final double x2p) {
	for (int i = 0; i < m; i++)
	    yv[i] = srp[i].interp(x2p);
	Spline_interp scol = new Spline_interp(x1, yv);
	return scol.interp(x1p);
    }

    private static int[] wt_d = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	    0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -3, 0, 0, 3, 0,
	    0, 0, 0, -2, 0, 0, -1, 0, 0, 0, 0, 2, 0, 0, -2, 0, 0, 0, 0, 1, 0,
	    0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -3, 0,
	    0, 3, 0, 0, 0, 0, -2, 0, 0, -1, 0, 0, 0, 0, 2, 0, 0, -2, 0, 0, 0,
	    0, 1, 0, 0, 1, -3, 3, 0, 0, -2, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	    0, 0, 0, 0, 0, 0, 0, 0, -3, 3, 0, 0, -2, -1, 0, 0, 9, -9, 9, -9, 6,
	    3, -3, -6, 6, -6, -3, 3, 4, 2, 1, 2, -6, 6, -6, 6, -4, -2, 2, 4,
	    -3, 3, 3, -3, -2, -1, -1, -2, 2, -2, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0,
	    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, -2, 0, 0, 1, 1, 0, 0, -6, 6,
	    -6, 6, -3, -3, 3, 3, -4, 4, 2, -2, -2, -2, -1, -1, 4, -4, 4, -4, 2,
	    2, -2, -2, 2, -2, -2, 2, 1, 1, 1, 1 };

    private static int[][] wt = buildMatrix(16, 16, wt_d);

    /**
     * Given arrays y[0..3], y1[0..3], y2[0..3], and y12[0..3], containing the
     * function, gradients, and cross-derivative at the four grid points of a
     * rectangular grid cell (numbered counterclockwise from the lower left),
     * and given d1 and d2, the length of the grid cell in the 1 and 2
     * directions, this routine returns the table c[0..3][0..3] that is used by
     * routine bcuint for bicubic interpolation.
     * 
     * @param y
     * @param y1
     * @param y2
     * @param y12
     * @param d1
     * @param d2
     * @param c
     */
    public static void bcucof(final double[] y, final double[] y1,
	    final double[] y2, final double[] y12, final double d1,
	    final double d2, final double[][] c) {
	int l, k, j, i;
	double xx, d1d2 = d1 * d2;
	double[] cl = new double[16];
	double[] x = new double[16];
	// static Matint wt(16,16,wt_d);
	for (i = 0; i < 4; i++) {
	    x[i] = y[i];
	    x[i + 4] = y1[i] * d1;
	    x[i + 8] = y2[i] * d2;
	    x[i + 12] = y12[i] * d1d2;
	}
	for (i = 0; i < 16; i++) {
	    xx = 0.0;
	    for (k = 0; k < 16; k++)
		xx += wt[i][k] * x[k];
	    cl[i] = xx;
	}
	l = 0;
	for (i = 0; i < 4; i++)
	    for (j = 0; j < 4; j++)
		c[i][j] = cl[l++];
    }

    /**
     * Bicubic interpolation within a grid square. Input quantities are
     * y,y1,y2,y12 (as described in bcucof); x1l and x1u, the lower and upper
     * coordinates of the grid square in the 1 direction; x2l and x2u likewise
     * for the 2 direction; and x1,x2, the coordinates of the desired point for
     * the interpolation. The interpolated function value is returned as ansy,
     * and the interpolated gradient values as ansy1 and ansy2. This routine
     * calls bcucof.
     * 
     * @param y
     * @param y1
     * @param y2
     * @param y12
     * @param x1l
     * @param x1u
     * @param x2l
     * @param x2u
     * @param x1
     * @param x2
     * @param ansy
     * @param ansy1
     * @param ansy2
     */
    public static void bcuint(final double[] y, final double[] y1,
	    final double[] y2, final double[] y12, final double x1l,
	    final double x1u, final double x2l, final double x2u,
	    final double x1, final double x2, final doubleW ansy,
	    final doubleW ansy1, final doubleW ansy2) {
	int i;
	double t, u, d1 = x1u - x1l, d2 = x2u - x2l;
	double[][] c = new double[4][4];
	bcucof(y, y1, y2, y12, d1, d2, c);
	if (x1u == x1l || x2u == x2l)
	    throw new IllegalArgumentException("Bad input in routine bcuint");
	t = (x1 - x1l) / d1;
	u = (x2 - x2l) / d2;
	ansy.val = ansy2.val = ansy1.val = 0.0;
	for (i = 3; i >= 0; i--) {
	    ansy.val = t * ansy.val + ((c[i][3] * u + c[i][2]) * u + c[i][1])
		    * u + c[i][0];
	    ansy2.val = t * ansy2.val + (3.0 * c[i][3] * u + 2.0 * c[i][2]) * u
		    + c[i][1];
	    ansy1.val = u * ansy1.val + (3.0 * c[3][i] * t + 2.0 * c[2][i]) * t
		    + c[1][i];
	}
	ansy1.val /= d1;
	ansy2.val /= d2;
    }
}
