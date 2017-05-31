package com.nr.interp;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

/**
 * Object for interpolating a curve specified by n points in dim dimensions.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Curve_interp {
    int dim, n, in;
    boolean cls;
    double[][] pts;
    double[] s;
    double[] ans;
    Spline_interp[] srp;

    public Curve_interp(final double[][] ptsin) {
	this(ptsin, false);
    }

    /**
     * The n x dim matrix ptsin inputs the data points. Input close as 0 for an
     * open curve, 1 for a closed curve. (For a closed curve, the last data
     * point should not duplicate the first — the algorithm will connect them.)
     * 
     * @param ptsin
     * @param close
     */
    public Curve_interp(final double[][] ptsin, final boolean close) {
	n = ptsin.length;
	dim = ptsin[0].length;
	in = close ? 2 * n : n;
	cls = close;
	pts = new double[dim][in];
	s = new double[in];
	ans = new double[dim];
	srp = new Spline_interp[dim];

	int i, ii, im, j, ofs;
	double ss, soff, db, de;
	ofs = close ? n / 2 : 0;
	s[0] = 0.;
	for (i = 0; i < in; i++) {
	    ii = (i - ofs + n) % n;
	    im = (ii - 1 + n) % n;
	    for (j = 0; j < dim; j++)
		pts[j][i] = ptsin[ii][j];
	    if (i > 0) {
		s[i] = s[i - 1] + rad(ptsin[ii], ptsin[im]);
		if (s[i] == s[i - 1])
		    throw new IllegalArgumentException("error in Curve_interp");
	    }
	}
	ss = close ? s[ofs + n] - s[ofs] : s[n - 1] - s[0];
	soff = s[ofs];
	for (i = 0; i < in; i++)
	    s[i] = (s[i] - soff) / ss;
	for (j = 0; j < dim; j++) {
	    db = in < 4 ? 1.e99 : fprime(s, 0, pts[j], 0, 1);
	    de = in < 4 ? 1.e99 : fprime(s, in - 1, pts[j], in - 1, -1);
	    srp[j] = new Spline_interp(s, pts[j], db, de);
	}
    }

    /**
     * Interpolate a point on the stored curve. The point is parameterized by t,
     * in the range [0,1]. For open curves, values of t outside this range will
     * return extrapolations (dangerous!). For closed curves, t is periodic with
     * period 1.
     * 
     * @param tt
     * @return
     */
    public double[] interp(final double tt) {
	double t = tt;
	if (cls)
	    t = t - floor(t);
	for (int j = 0; j < dim; j++)
	    ans[j] = srp[j].interp(t);
	return ans;
    }

    /**
     * Utility for estimating the derivatives at the endpoints. x and y point to
     * the abscissa and ordinate of the endpoint. If pm is C1, points to the
     * right will be used (left endpoint); if it is 1, points to the left will
     * be used (right endpoint).
     * 
     * @param x
     * @param off_x
     * @param y
     * @param off_y
     * @param pm
     * @return
     */
    private double fprime(final double[] x, final int off_x, final double[] y,
	    final int off_y, final int pm) {
	double s1 = x[off_x + 0] - x[off_x + pm * 1], s2 = x[off_x + 0]
		- x[off_x + pm * 2], s3 = x[off_x + 0] - x[off_x + pm * 3], s12 = s1
		- s2, s13 = s1 - s3, s23 = s2 - s3;
	return -(s1 * s2 / (s13 * s23 * s3)) * y[off_y + pm * 3]
		+ (s1 * s3 / (s12 * s2 * s23)) * y[off_y + pm * 2]
		- (s2 * s3 / (s1 * s12 * s13)) * y[off_y + pm * 1]
		+ (1. / s1 + 1. / s2 + 1. / s3) * y[off_y + 0];
    }

    private double rad(final double[] p1, final double[] p2) {
	double sum = 0.;
	for (int i = 0; i < dim; i++)
	    sum += SQR(p1[i] - p2[i]);
	return sqrt(sum);
    }

}
