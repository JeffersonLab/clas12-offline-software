package com.nr.interp;

/**
 * Cubic Spline Interpolation
 * 
 * Cubic spline interpolation object. Construct with x and y vectors, and
 * (optionally) values of the first derivative at the endpoints, then call
 * interp for interpolated values
 * 
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Spline_interp extends Base_interp {
    double[] y2;

    public Spline_interp(final double[] xv, final double[] yv) {
	this(xv, yv, 1.e99, 1.e99);
    }

    public Spline_interp(final double[] xv, final double[] yv,
	    final double yp1, final double ypn) { // double yp1=1.e99, double
						  // ypn=1.e99
	super(xv, yv, 2);
	y2 = new double[xv.length];
	sety2(xv, yv, yp1, ypn);
    }

    public void sety2(final double[] xv, final double[] yv) {
	sety2(xv, yv, 1.e99, 1.e99);
    }

    /**
     * 
     * This routine stores an array y2[0..n-1] with second derivatives of the
     * interpolating function at the tabulated points pointed to by xv, using
     * function values pointed to by yv. If yp1 and/or ypn are equal to 1.0E99
     * or larger, the routine is signaled to set the corresponding boundary
     * condition for a natural spline, with zero second derivative on that
     * boundary; otherwise, they are the values of the first derivatives at the
     * endpoints.
     * 
     * @param xv
     * @param yv
     * @param yp1
     * @param ypn
     */
    public void sety2(final double[] xv, final double[] yv, final double yp1,
	    final double ypn) { // double yp1=1.e99, double ypn=1.e99
	int i, k;
	double p, qn, sig, un;
	double[] u = new double[n - 1];
	if (yp1 > 0.99e99)
	    y2[0] = u[0] = 0.0;
	else {
	    y2[0] = -0.5;
	    u[0] = (3.0 / (xv[1] - xv[0]))
		    * ((yv[1] - yv[0]) / (xv[1] - xv[0]) - yp1);
	}
	for (i = 1; i < n - 1; i++) {
	    sig = (xv[i] - xv[i - 1]) / (xv[i + 1] - xv[i - 1]);
	    p = sig * y2[i - 1] + 2.0;
	    y2[i] = (sig - 1.0) / p;
	    u[i] = (yv[i + 1] - yv[i]) / (xv[i + 1] - xv[i])
		    - (yv[i] - yv[i - 1]) / (xv[i] - xv[i - 1]);
	    u[i] = (6.0 * u[i] / (xv[i + 1] - xv[i - 1]) - sig * u[i - 1]) / p;
	}
	if (ypn > 0.99e99)
	    qn = un = 0.0;
	else {
	    qn = 0.5;
	    un = (3.0 / (xv[n - 1] - xv[n - 2]))
		    * (ypn - (yv[n - 1] - yv[n - 2]) / (xv[n - 1] - xv[n - 2]));
	}
	y2[n - 1] = (un - qn * u[n - 2]) / (qn * y2[n - 2] + 1.0);
	for (k = n - 2; k >= 0; k--)
	    y2[k] = y2[k] * y2[k + 1] + u[k];
    }

    /**
     * Given a value x, and using pointers to data xx and yy, this routine
     * returns an interpolated value y, and stores an error estimate dy. The
     * returned value is obtained by mm-point polynomial interpolation on the
     * subrange xx[jl..jl+mm-1].
     */
    @Override
    public double rawinterp(final int jl, final double x) {
	int klo = jl, khi = jl + 1;
	double y, h, b, a;
	h = xx[khi] - xx[klo];
	if (h == 0.0)
	    throw new IllegalArgumentException("Bad input to routine splint");
	a = (xx[khi] - x) / h;
	b = (x - xx[klo]) / h;
	y = a * yy[klo] + b * yy[khi]
		+ ((a * a * a - a) * y2[klo] + (b * b * b - b) * y2[khi])
		* (h * h) / 6.0;
	return y;
    }

}
